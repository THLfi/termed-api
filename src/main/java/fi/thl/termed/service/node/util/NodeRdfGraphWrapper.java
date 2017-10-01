package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.FunctionUtils.memoize;
import static java.util.Optional.ofNullable;
import static org.apache.jena.graph.Node.ANY;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByProperty;
import fi.thl.termed.service.node.specification.NodesByReference;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRdfGraphWrapper extends GraphBase {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Function<Specification<NodeId, Node>, Stream<Node>> nodeProvider;

  // caches
  private Map<TypeId, Type> types = new HashMap<>();
  private Map<TextAttributeId, TextAttribute> textAttributes = new HashMap<>();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = new HashMap<>();

  private Function<Node, Stream<Triple>> toTriples;

  public NodeRdfGraphWrapper(List<Type> typeList,
      Function<Specification<NodeId, Node>, Stream<Node>> nodeProvider) {

    typeList.forEach(type -> {
      types.put(type.identifier(), type);
      type.getTextAttributes().forEach(a -> textAttributes.put(a.identifier(), a));
      type.getReferenceAttributes().forEach(a -> referenceAttributes.put(a.identifier(), a));
    });

    this.nodeProvider = nodeProvider;

    Function<NodeId, String> uriProvider = nodeId -> nodeProvider.apply(
        new AndSpecification<>(
            new NodesByGraphId(nodeId.getTypeGraphId()),
            new NodesByTypeId(nodeId.getTypeId()),
            new NodeById(nodeId.getId()))).findFirst()
        .map(node -> ofNullable(node.getUri()).orElse("urn:uuid:" + node.getId()))
        .orElse("urn:uuid:" + nodeId.getId());

    this.toTriples = new NodeToTriples(typeList, memoize(uriProvider, 100_000));
  }

  @Override
  protected ExtendedIterator<Triple> graphBaseFind(Triple match) {
    if (log.isTraceEnabled()) {
      log.trace("Find {}", match);
    }

    org.apache.jena.graph.Node subject = nullToAny(match.getMatchSubject());
    org.apache.jena.graph.Node predicate = nullToAny(match.getMatchPredicate());
    org.apache.jena.graph.Node object = nullToAny(match.getMatchObject());

    if (!subject.isURI() && !subject.equals(ANY)) {
      return WrappedIterator.emptyIterator();
    }

    if (subject.isURI()) {
      return findBySubject(subject.getURI()).filterKeep(match::matches);
    }

    if (predicate.equals(RDF.type.asNode()) && object.isURI()) {
      return findByType(object.getURI()).filterKeep(match::matches);
    }

    if (object.isURI()) {
      String predicateUri = predicate.isURI() ? predicate.getURI() : null;
      return findByObject(predicateUri, object.getURI()).filterKeep(match::matches);
    }

    if (object.isLiteral()) {
      String predicateUri = predicate.isURI() ? predicate.getURI() : null;
      return findByLiteral(predicateUri, object.getLiteralLexicalForm()).filterKeep(match::matches);
    }

    if (log.isTraceEnabled()) {
      log.trace("Full scan");
    }

    return findAll().filterKeep(match::matches);
  }

  private org.apache.jena.graph.Node nullToAny(org.apache.jena.graph.Node node) {
    return node == null ? ANY : node;
  }

  private ExtendedIterator<Triple> findBySubject(String subjectUri) {
    Specification<NodeId, Node> nodeSpec = types.values().stream()
        .map(type -> new AndSpecification<>(
            new NodesByGraphId(type.getGraphId()),
            new NodesByTypeId(type.getId()),
            byUriOrId(subjectUri)))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeProvider.apply(nodeSpec).flatMap(toTriples).iterator());
  }

  private Specification<NodeId, fi.thl.termed.domain.Node> byUriOrId(String nodeUri) {
    return nodeUri.matches(RegularExpressions.URN_UUID) ?
        new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))) :
        new NodesByUri(nodeUri);
  }

  private ExtendedIterator<Triple> findByType(String typeUri) {
    Optional<TypeId> typeOptional = types.values().stream()
        .filter(type -> Objects.equals(type.getUri(), typeUri))
        .map(Type::identifier).findFirst();

    if (!typeOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, Node> nodeSpec = new AndSpecification<>(
        new NodesByGraphId(typeOptional.get().getGraphId()),
        new NodesByTypeId(typeOptional.get().getId()));

    return WrappedIterator.create(nodeProvider.apply(nodeSpec).flatMap(toTriples).iterator());
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByObject(String predicateUri, String valueUri) {
    Optional<NodeId> valueOptional = nodeProvider.apply(byUriOrId(valueUri))
        .findFirst().map(NodeId::new);

    if (!valueOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, Node> nodeSpec = referenceAttributes.values().stream()
        .filter(refAttr -> predicateUri == null || Objects.equals(refAttr.getUri(), predicateUri))
        .map(refAttr -> new AndSpecification<>(
            new NodesByGraphId(refAttr.getDomainGraphId()),
            new NodesByTypeId(refAttr.getDomainId()),
            new NodesByReference(refAttr.getId(), valueOptional.get().getId())))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeProvider.apply(nodeSpec).flatMap(toTriples).iterator());
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByLiteral(String predicateUri, String value) {
    Specification<NodeId, Node> nodeSpec = textAttributes.values().stream()
        .filter(textAttr -> predicateUri == null || Objects.equals(textAttr.getUri(), predicateUri))
        .map(textAttr -> new AndSpecification<>(
            new NodesByGraphId(textAttr.getDomainGraphId()),
            new NodesByTypeId(textAttr.getDomainId()),
            new NodesByProperty(textAttr.getId(), value)))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeProvider.apply(nodeSpec).flatMap(toTriples).iterator());
  }

  private ExtendedIterator<Triple> findAll() {
    Specification<NodeId, Node> nodeSpec = types.values().stream()
        .map(type -> new AndSpecification<>(
            new NodesByGraphId(type.getGraphId()),
            new NodesByTypeId(type.getId())))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeProvider.apply(nodeSpec).flatMap(toTriples).iterator());
  }

}
