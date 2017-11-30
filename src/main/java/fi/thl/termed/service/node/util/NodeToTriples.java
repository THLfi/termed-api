package fi.thl.termed.service.node.util;

import static fi.thl.termed.domain.DefaultUris.uri;
import static fi.thl.termed.util.collect.FunctionUtils.memoize;
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

  private Function<TypeId, String> typeResolver;
  private Function<TextAttributeId, String> textAttrResolver;
  private Function<ReferenceAttributeId, String> refAttrResolver;
  private Function<NodeId, String> nodeResolver;

  public NodeToTriples(
      Function<TypeId, Optional<String>> typeUris,
      Function<TextAttributeId, Optional<String>> textAttrUris,
      Function<ReferenceAttributeId, Optional<String>> refAttrUris,
      Function<NodeId, Optional<String>> nodeUris) {
    // cache and add urn fallback to uri resolving functions
    this.typeResolver = memoize(id -> typeUris.apply(id).orElse(uri(id)), 1000);
    this.textAttrResolver = memoize(id -> textAttrUris.apply(id).orElse(uri(id)), 1000);
    this.refAttrResolver = memoize(id -> refAttrUris.apply(id).orElse(uri(id)), 1000);
    this.nodeResolver = memoize(id -> nodeUris.apply(id).orElse(uri(id)), 100_000);
  }

  @Override
  public List<Triple> apply(Node node) {
    List<Triple> triples = new ArrayList<>();

    org.apache.jena.graph.Node subject = createURI(nodeResolver.apply(node.identifier()));

    triples.add(create(subject, RDF.type.asNode(), createURI(typeResolver.apply(node.getType()))));

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

}
