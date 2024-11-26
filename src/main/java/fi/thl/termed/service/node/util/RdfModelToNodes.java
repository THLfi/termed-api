package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.RegularExpressions.URN_UUID;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Ascii;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.Node.Builder;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.StringUtils;
import fi.thl.termed.util.URIs;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.OptionalUtils;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.vocabulary.RDF;

/**
 * Function to transform rdf model into list of nodes conforming to provided graph.
 */
public class RdfModelToNodes implements Function<RdfModel, List<Node>> {

  private List<Type> types;
  private Function<Specification<NodeId, Node>, Optional<NodeId>> nodeIdProvider;
  private boolean importCodes;

  public RdfModelToNodes(List<Type> types,
      Function<Query<NodeId, Node>, Stream<NodeId>> nodeIdProvider,
      boolean importCodes) {
    this.types = types.stream().filter(t -> t.getUri().isPresent()).collect(toList());
    this.nodeIdProvider = (specification) -> StreamUtils
        .findFirstAndClose(nodeIdProvider.apply(new Query<>(specification, emptyList(), -1)));
    this.importCodes = importCodes;
  }

  @Override
  public List<Node> apply(RdfModel rdfModel) {
    Map<String, Node.Builder> nodes = Maps.newLinkedHashMap();

    // init nodes
    for (Type type : types) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri().orElse(null))) {
        String uri = r.getUri();

        Node.Builder nodeBuilder = Node.builder()
            .id(findExistingIdForUriOrCreateNew(type.identifier(), uri))
            .uri(uri)
            .code(importCodes ? StringUtils.normalize(URIs.localName(uri)) : null);

        nodes.put(r.getUri(), nodeBuilder);
      }
    }

    // populate attributes
    for (Type type : types) {
      for (RdfResource rdfResource : rdfModel.find(RDF.type.getURI(), type.getUri().orElse(null))) {
        Node.Builder nodeBuilder = nodes.get(rdfResource.getUri());

        setTextAttrValues(type, nodeBuilder, rdfResource);
        setRefAttrValues(type, nodeBuilder, rdfResource, nodes);
      }
    }

    return nodes.values().stream()
        .map(Builder::build)
        .collect(toImmutableList());
  }

  private void setTextAttrValues(Type type, Node.Builder node, RdfResource rdfResource) {
    for (TextAttribute textAttribute : type.getTextAttributes()) {
      for (LangValue langValues : rdfResource.getLiterals(textAttribute.getUri().orElse(null))) {
        node.addProperty(textAttribute.getId(),
            new StrictLangValue(
                Ascii.truncate(langValues.getLang(), 2, ""),
                langValues.getValue(),
                textAttribute.getRegex()));
      }
    }
  }

  private void setRefAttrValues(Type type, Node.Builder node, RdfResource rdfResource,
      Map<String, Node.Builder> nodes) {
    for (ReferenceAttribute refAttribute : type.getReferenceAttributes()) {
      for (String objectUri : rdfResource.getObjects(refAttribute.getUri().orElse(null))) {
        if (nodes.containsKey(objectUri)) {
          Node object = nodes.get(objectUri).build();
          if (object.getType().equals(refAttribute.getRange())) {
            node.addReference(refAttribute.getId(), object.identifier());
          }
        } else {
          Optional<NodeId> objectId = findExistingIdForUri(refAttribute.getRange(), objectUri);
          objectId.ifPresent(o -> node.addReference(refAttribute.getId(), o));
        }
      }
    }
  }

  private NodeId findExistingIdForUriOrCreateNew(TypeId type, String uri) {
    return OptionalUtils.lazyFindFirst(
        () -> findExistingIdForUri(type, uri),
        () -> extractUrnUuid(uri).map(uuid -> NodeId.of(uuid, type)))
        .orElseGet(() -> NodeId.of(nameUUIDFromString(uri), type));
  }

  private Optional<NodeId> findExistingIdForUri(TypeId type, String uri) {
    return OptionalUtils.lazyFindFirst(
        () -> resolveByUri(type, uri),
        () -> resolveByUrnUuid(type, uri),
        () -> resolveByNameUuid(type, uri));
  }

  private Optional<NodeId> resolveByUri(TypeId type, String uri) {
    return nodeIdProvider.apply(AndSpecification.and(
        NodesByGraphId.of(type.getGraphId()),
        NodesByTypeId.of(type.getId()),
        NodesByUri.of(uri)));
  }

  private Optional<NodeId> resolveByUrnUuid(TypeId type, String uri) {
    return extractUrnUuid(uri).flatMap(uuid ->
        nodeIdProvider.apply(AndSpecification.and(
            NodesByGraphId.of(type.getGraphId()),
            NodesByTypeId.of(type.getId()),
            NodesById.of(uuid))));
  }

  private Optional<UUID> extractUrnUuid(String uri) {
    return uri.matches(URN_UUID)
        ? Optional.of(UUIDs.fromString(uri.substring("urn:uuid:".length())))
        : Optional.empty();
  }

  private Optional<NodeId> resolveByNameUuid(TypeId type, String uri) {
    return nodeIdProvider.apply(AndSpecification.and(
        NodesByGraphId.of(type.getGraphId()),
        NodesByTypeId.of(type.getId()),
        NodesById.of(UUIDs.nameUUIDFromString(uri))));
  }

}
