package fi.thl.termed.service.node.util;

import static fi.thl.termed.service.node.util.UriResolvers.nodeUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.refAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.textAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.typeUriResolver;
import static fi.thl.termed.util.collect.StreamUtils.findFirstAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.OrSpecification.or;
import static java.util.stream.Collectors.toList;
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
import fi.thl.termed.service.node.specification.NodesByPropertyString;
import fi.thl.termed.service.node.specification.NodesByReference;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.Specification;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ClosableIterator;
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

  private Function<Node, List<Triple>> toTriples;

  public NodeRdfGraphWrapper(List<Type> typeList,
      Function<Specification<NodeId, Node>, Stream<Node>> nodeProvider) {

    typeList.forEach(type -> {
      types.put(type.identifier(), type);
      type.getTextAttributes().forEach(a -> textAttributes.put(a.identifier(), a));
      type.getReferenceAttributes().forEach(a -> referenceAttributes.put(a.identifier(), a));
    });

    this.nodeProvider = nodeProvider;

    Function<TypeId, Optional<Type>> getType = id -> typeList.stream()
        .filter(t -> t.identifier().equals(id)).findFirst();
    Function<NodeId, Optional<Node>> getNode = nodeId ->
        findFirstAndClose(nodeProvider.apply(and(
            new NodesByGraphId(nodeId.getTypeGraphId()),
            new NodesByTypeId(nodeId.getTypeId()),
            new NodeById(nodeId.getId()))));

    this.toTriples = new NodeToTriples(
        typeUriResolver(getType),
        textAttrUriResolver(getType),
        refAttrUriResolver(getType),
        nodeUriResolver(getNode));
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
    Specification<NodeId, Node> nodeSpec = or(types.values().stream()
        .map(type -> and(
            new NodesByGraphId(type.getGraphId()),
            new NodesByTypeId(type.getId()),
            byUriOrId(subjectUri)))
        .collect(toList()));

    return nodeStreamToTriples(nodeProvider.apply(nodeSpec));
  }

  private Specification<NodeId, fi.thl.termed.domain.Node> byUriOrId(String nodeUri) {
    return nodeUri.matches(RegularExpressions.URN_UUID) ?
        new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))) :
        new NodesByUri(nodeUri);
  }

  private ExtendedIterator<Triple> findByType(String typeUri) {
    Optional<TypeId> typeOptional = types.values().stream()
        .filter(type -> Objects.equals(type.getUri().orElse(null), typeUri))
        .map(Type::identifier).findFirst();

    if (!typeOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, Node> nodeSpec = and(
        new NodesByGraphId(typeOptional.get().getGraphId()),
        new NodesByTypeId(typeOptional.get().getId()));

    return nodeStreamToTriples(nodeProvider.apply(nodeSpec));
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByObject(String predicateUri, String valueUri) {
    Optional<NodeId> valueOptional = findFirstAndClose(nodeProvider.apply(byUriOrId(valueUri)))
        .map(NodeId::new);

    if (!valueOptional.isPresent()) {
      return WrappedIterator.emptyIterator();
    }

    Specification<NodeId, Node> nodeSpec = or(referenceAttributes.values().stream()
        .filter(refAttr -> predicateUri == null ||
            Objects.equals(refAttr.getUri().orElse(null), predicateUri))
        .map(refAttr -> and(
            new NodesByGraphId(refAttr.getDomainGraphId()),
            new NodesByTypeId(refAttr.getDomainId()),
            new NodesByReference(refAttr.getId(), valueOptional.get().getId())))
        .collect(toList()));

    return nodeStreamToTriples(nodeProvider.apply(nodeSpec));
  }

  // predicateUri can be null
  private ExtendedIterator<Triple> findByLiteral(String predicateUri, String value) {
    Specification<NodeId, Node> nodeSpec = or(textAttributes.values().stream()
        .filter(textAttr -> predicateUri == null ||
            Objects.equals(textAttr.getUri().orElse(null), predicateUri))
        .map(textAttr -> and(
            new NodesByGraphId(textAttr.getDomainGraphId()),
            new NodesByTypeId(textAttr.getDomainId()),
            new NodesByPropertyString(textAttr.getId(), value)))
        .collect(toList()));

    return nodeStreamToTriples(nodeProvider.apply(nodeSpec));
  }

  private ExtendedIterator<Triple> findAll() {
    Specification<NodeId, Node> nodeSpec = or(types.values().stream()
        .map(type -> and(
            new NodesByGraphId(type.getGraphId()),
            new NodesByTypeId(type.getId())))
        .collect(toList()));

    return nodeStreamToTriples(nodeProvider.apply(nodeSpec));
  }

  private ExtendedIterator<Triple> nodeStreamToTriples(Stream<Node> stream) {
    Iterator<Triple> streamIterator = stream.flatMap(n -> toTriples.apply(n).stream()).iterator();

    ClosableIterator<Triple> closableStreamIterator = new ClosableIterator<Triple>() {
      @Override
      public boolean hasNext() {
        try {
          return streamIterator.hasNext();
        } catch (RuntimeException | Error e) {
          close();
          throw e;
        }
      }

      @Override
      public Triple next() {
        try {
          return streamIterator.next();
        } catch (RuntimeException | Error e) {
          close();
          throw e;
        }
      }

      @Override
      public void close() {
        log.trace("Close node stream");
        stream.close();
      }
    };

    return WrappedIterator.create(closableStreamIterator);
  }

}
