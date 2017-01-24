package fi.thl.termed.web.external.node.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphDto;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.TypeId;

import static com.google.common.base.Strings.isNullOrEmpty;

public class NodeToDtoMapper implements BiFunction<Node, NodeQuery, NodeDto> {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  private Function<GraphId, Graph> graphProvider;
  private Function<TypeId, Type> typeProvider;
  private BiFunction<Node, String, List<Node>> nodeReferenceProvider;
  private BiFunction<Node, String, List<Node>> nodeReferrerProvider;

  public NodeToDtoMapper(
      Function<GraphId, Graph> graphProvider,
      Function<TypeId, Type> typeProvider,
      BiFunction<Node, String, List<Node>> nodeReferenceProvider,
      BiFunction<Node, String, List<Node>> nodeReferrerProvider) {
    this.graphProvider = graphProvider;
    this.typeProvider = typeProvider;
    this.nodeReferenceProvider = nodeReferenceProvider;
    this.nodeReferrerProvider = nodeReferrerProvider;
  }

  public NodeDto apply(Node node, NodeQuery query) {
    return nodeToDto(node, new HashSet<>(), 0, query.select, query.recurse);
  }

  private NodeDto nodeToDto(Node node, Set<NodeId> visited, int depth,
                            NodeQuery.Select select, NodeQuery.Recurse recurse) {

    visited.add(node.identifier());

    NodeDto dto = new NodeDto();
    Type type = typeProvider.apply(node.getType());

    dto.setId(node.getId());
    dto.setUri(node.getUri());
    dto.setCode(node.getCode());

    dto.setType(typeDto(type, select));

    if (select.audit) {
      dto.setCreatedBy(node.getCreatedBy());
      dto.setCreatedDate(node.getCreatedDate());
      dto.setLastModifiedBy(node.getLastModifiedBy());
      dto.setLastModifiedDate(node.getLastModifiedDate());
    }

    dto.setProperties(mapProperties(node.getProperties(), select));

    dto.setReferences(Multimaps.transformValues(
        filterAndLoadReferences(node, visited, depth, select, recurse), ref ->
            nodeToDto(ref, visited, depth + 1, select, recurse)));

    dto.setReferrers(Multimaps.transformValues(
        filterAndLoadReferrers(node, visited, depth, select, recurse), ref ->
            nodeToDto(ref, visited, depth + 1, select, recurse)));

    return dto;
  }

  private Multimap<String, LangValue> mapProperties(Multimap<String, StrictLangValue> properties,
                                                    NodeQuery.Select select) {

    Multimap<String, LangValue> mappedProperties =
        Multimaps.transformValues(properties, LangValue::new);

    if (!select.properties.isEmpty()) {
      mappedProperties = Multimaps.filterKeys(
          mappedProperties, key -> select.properties.contains(key));
    }

    return mappedProperties;
  }

  private Multimap<String, Node> filterAndLoadReferences(Node node, Set<NodeId> visited, int depth,
                                                         NodeQuery.Select select,
                                                         NodeQuery.Recurse recurse) {

    // remove keys that have all values visited
    Multimap<String, NodeId> unfiltered = node.getReferences();
    Multimap<String, NodeId> references =
        Multimaps.filterKeys(unfiltered, key -> !visited.containsAll(unfiltered.get(key)));

    if (!select.references.isEmpty()) {
      references = Multimaps.filterKeys(references, key -> select.references.contains(key));
    }

    // load values that are withing given recursion bounds
    Multimap<String, Node> loadedReferences = LinkedHashMultimap.create();
    references.keySet().stream()
        .filter(key -> depth <= recurse.references.getOrDefault(key, 0))
        .forEach(key -> loadedReferences.putAll(key, nodeReferenceProvider.apply(node, key)));

    return LinkedHashMultimap.create(
        Multimaps.filterValues(loadedReferences, value -> !visited.contains(value.identifier())));
  }

  private Multimap<String, Node> filterAndLoadReferrers(Node node, Set<NodeId> visited, int depth,
                                                        NodeQuery.Select select,
                                                        NodeQuery.Recurse recurse) {

    // remove keys that have all values visited
    Multimap<String, NodeId> unfiltered = node.getReferrers();
    Multimap<String, NodeId> referrers =
        Multimaps.filterKeys(unfiltered, key -> !visited.containsAll(unfiltered.get(key)));

    if (!select.referrers.isEmpty()) {
      referrers = Multimaps.filterKeys(referrers, key -> select.referrers.contains(key));
    }

    // load values that are withing given recursion bounds
    Multimap<String, Node> loadedReferrers = LinkedHashMultimap.create();
    referrers.keySet().stream()
        .filter(key -> depth <= recurse.referrers.getOrDefault(key, 0))
        .forEach(key -> loadedReferrers.putAll(key, nodeReferrerProvider.apply(node, key)));

    return LinkedHashMultimap.create(
        Multimaps.filterValues(loadedReferrers, value -> !visited.contains(value.identifier())));
  }

  private TypeDto typeDto(Type type, NodeQuery.Select select) {
    TypeDto dto = new TypeDto();

    dto.setId(type.getId());
    dto.setUri(type.getUri());
    dto.setGraph(graphDto(type.getGraph(), select));

    dto.setTextAttributes(
        type.getTextAttributes().stream()
            .filter(attr -> !isNullOrEmpty(attr.getUri()))
            .collect(Collectors.toMap(TextAttribute::getId, TextAttribute::getUri)));
    dto.setReferenceAttributes(
        type.getReferenceAttributes().stream()
            .filter(attr -> !isNullOrEmpty(attr.getUri()))
            .collect(Collectors.toMap(ReferenceAttribute::getId, ReferenceAttribute::getUri)));

    if (select.type) {
      dto.setProperties(type.getProperties());
    }

    return dto;
  }

  private GraphDto graphDto(GraphId graphId, NodeQuery.Select select) {
    GraphDto dto = new GraphDto();
    Graph g = graphProvider.apply(graphId);

    dto.setId(graphId.getId());
    dto.setUri(g.getUri());
    dto.setCode(g.getCode());

    if (select.graph) {
      dto.setProperties(g.getProperties());
    }

    return dto;
  }

}
