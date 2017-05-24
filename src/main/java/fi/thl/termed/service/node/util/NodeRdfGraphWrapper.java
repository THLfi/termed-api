package fi.thl.termed.service.node.util;

import static fi.thl.termed.util.FunctionUtils.memoize;
import static fi.thl.termed.util.FunctionUtils.partialApplySecond;

import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByProperty;
import fi.thl.termed.service.node.specification.NodesByReference;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRdfGraphWrapper extends GraphBase {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<NodeId, fi.thl.termed.domain.Node> nodeService;

  private UUID graphId;
  private User user;

  // caches
  private Map<TypeId, Type> types = new HashMap<>();
  private Map<TextAttributeId, TextAttribute> textAttributes = new HashMap<>();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = new HashMap<>();

  private Function<fi.thl.termed.domain.Node, Stream<Triple>> toTriples;

  public NodeRdfGraphWrapper(Service<NodeId, fi.thl.termed.domain.Node> nodeService,
      Service<TypeId, Type> typeService, UUID graphId, User user, boolean useUuidUris) {
    this.nodeService = nodeService;
    this.graphId = graphId;
    this.user = user;

    this.toTriples = new NodeToTriples(
        memoize(partialApplySecond(typeService::get, user)),
        nodeId -> nodeService.get(new AndSpecification<>(
            new NodesByGraphId(nodeId.getTypeGraphId()),
            new NodesByTypeId(nodeId.getTypeId()),
            new NodeById(nodeId.getId())), user).findFirst(), useUuidUris);

    typeService.get(new TypesByGraphId(graphId), user).forEach(type -> {
      types.put(type.identifier(), type);
      type.getTextAttributes().forEach(a -> textAttributes.put(a.identifier(), a));
      type.getReferenceAttributes().forEach(a -> referenceAttributes.put(a.identifier(), a));
    });
  }

  @Override
  protected ExtendedIterator<Triple> graphBaseFind(Triple match) {
    if (log.isTraceEnabled()) {
      log.trace("Find {}", match);
    }

    Node subject = nullToAny(match.getMatchSubject());
    Node predicate = nullToAny(match.getMatchPredicate());
    Node object = nullToAny(match.getMatchObject());

    if (!subject.isURI() && !subject.equals(Node.ANY)) {
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

  private Node nullToAny(Node node) {
    return node == null ? Node.ANY : node;
  }

  private ExtendedIterator<Triple> findBySubject(String subjectUri) {
    Specification<NodeId, fi.thl.termed.domain.Node> nodeSpec = new AndSpecification<>(
        new NodesByGraphId(graphId),
        byUriOrId(subjectUri));

    return WrappedIterator.create(nodeService.get(nodeSpec, user).flatMap(toTriples).iterator());
  }

  private Specification<NodeId, fi.thl.termed.domain.Node> byUriOrId(String nodeUri) {
    return nodeUri.matches(RegularExpressions.URN_UUID) ?
        new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))) :
        new NodesByUri(nodeUri);
  }

  private ExtendedIterator<Triple> findByType(String typeUri) {
    Optional<TypeId> typeOptional = types.values().stream()
        .filter(type -> Objects.equals(type.getUri(), typeUri))
        .map(TypeId::new).findFirst();

    if (!typeOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, fi.thl.termed.domain.Node> nodeSpec = new AndSpecification<>(
        new NodesByGraphId(graphId), new NodesByTypeId(typeOptional.get().getId()));

    return WrappedIterator.create(nodeService.get(nodeSpec, user).flatMap(toTriples).iterator());
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByObject(String predicateUri, String valueUri) {
    Optional<NodeId> valueOptional = nodeService.get(new AndSpecification<>(
        new NodesByGraphId(graphId),
        byUriOrId(valueUri)), user).findFirst().map(NodeId::new);

    if (!valueOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, fi.thl.termed.domain.Node> nodeSpec = referenceAttributes.values()
        .stream()
        .filter(refAttr -> predicateUri == null || Objects.equals(refAttr.getUri(), predicateUri))
        .map(refAttr -> new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(refAttr.getDomainId()),
            new NodesByReference(refAttr.getId(), valueOptional.get().getId())))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeService.get(nodeSpec, user).flatMap(toTriples).iterator());
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByLiteral(String predicateUri, String value) {
    Specification<NodeId, fi.thl.termed.domain.Node> nodeSpec = textAttributes.values()
        .stream()
        .filter(textAttr -> predicateUri == null || Objects.equals(textAttr.getUri(), predicateUri))
        .map(textAttr -> new AndSpecification<>(
            new NodesByGraphId(graphId),
            new NodesByTypeId(textAttr.getDomainId()),
            new NodesByProperty(textAttr.getId(), value)))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return WrappedIterator.create(nodeService.get(nodeSpec, user).flatMap(toTriples).iterator());
  }

  private ExtendedIterator<Triple> findAll() {
    Specification<NodeId, fi.thl.termed.domain.Node> nodeSpec = new NodesByGraphId(graphId);
    return WrappedIterator.create(nodeService.get(nodeSpec, user).flatMap(toTriples).iterator());
  }

}
