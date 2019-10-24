package fi.thl.termed.service.node.util;

import static fi.thl.termed.domain.DefaultUris.propertyUri;
import static fi.thl.termed.domain.DefaultUris.uri;
import static fi.thl.termed.util.collect.FunctionUtils.memoize;
import static fi.thl.termed.util.collect.FunctionUtils.memoizeSoft;
import static java.util.Optional.ofNullable;
import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.graph.Triple.create;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

public class NodeToTriples implements Function<Node, List<Triple>> {

  private String defaultNamespace;
  private Function<TypeId, String> typeResolver;
  private Function<TextAttributeId, String> textAttrResolver;
  private Function<ReferenceAttributeId, String> refAttrResolver;
  private Function<NodeId, String> nodeResolver;
  private Function<String, org.apache.jena.graph.Node> createProperty;

  public NodeToTriples(
      String ns,
      Function<TypeId, Optional<String>> typeUris,
      Function<TextAttributeId, Optional<String>> textAttrUris,
      Function<ReferenceAttributeId, Optional<String>> refAttrUris,
      Function<NodeId, Optional<String>> nodeUris) {
    this.defaultNamespace = ns;
    // cache and add urn fallback to uri resolving functions
    this.typeResolver = memoize(id -> typeUris.apply(id).orElse(uri(ns, id)));
    this.textAttrResolver = memoize(id -> textAttrUris.apply(id).orElse(uri(ns, id)));
    this.refAttrResolver = memoize(id -> refAttrUris.apply(id).orElse(uri(ns, id)));
    this.nodeResolver = memoizeSoft(id -> nodeUris.apply(id).orElse(uri(ns, id)));
    this.createProperty = memoize(
        (propertyId) -> createURI(propertyUri(defaultNamespace, propertyId)));
  }

  @Override
  public List<Triple> apply(Node node) {
    List<Triple> triples = new ArrayList<>();

    org.apache.jena.graph.Node subject = createURI(nodeResolver.apply(node.identifier()));

    triples.add(create(subject, RDF.type.asNode(), createURI(typeResolver.apply(node.getType()))));

    triples.add(createTermedLiteral(subject, "id", node.getId().toString()));
    triples.add(createTermedLiteral(subject, "type", node.getTypeId()));
    triples.add(createTermedLiteral(subject, "graph", node.getTypeGraphId().toString()));
    node.getUri().ifPresent(s -> triples.add(createTermedLiteral(subject, "uri", s)));
    node.getCode().ifPresent(s -> triples.add(createTermedLiteral(subject, "code", s)));
    ofNullable(node.getNumber()).ifPresent(l ->
        triples.add(createTermedLiteral(subject, "number", l.toString())));
    ofNullable(node.getCreatedBy()).ifPresent(s ->
        triples.add(createTermedLiteral(subject, "createdBy", s)));
    ofNullable(node.getCreatedDate()).ifPresent(d ->
        triples.add(createTermedLiteral(subject, "createdDate", d.toString())));
    ofNullable(node.getLastModifiedBy()).ifPresent(s ->
        triples.add(createTermedLiteral(subject, "lastModifiedBy", s)));
    ofNullable(node.getLastModifiedDate()).ifPresent(d ->
        triples.add(createTermedLiteral(subject, "lastModifiedDate", d.toString())));

    node.getProperties().forEach((k, v) ->
        triples.add(create(
            subject,
            createURI(textAttrResolver.apply(new TextAttributeId(node.getType(), k))),
            createLiteral(v.getValue(), v.getLang()))));

    node.getReferences().forEach((k, v) ->
        triples.add(create(
            subject,
            createURI(refAttrResolver.apply(new ReferenceAttributeId(node.getType(), k))),
            createURI(nodeResolver.apply(v)))));

    return triples;
  }

  private Triple createTermedLiteral(org.apache.jena.graph.Node subject, String propertyId,
      String literal) {
    return create(subject, createProperty.apply(propertyId), createLiteral(literal));
  }

}
