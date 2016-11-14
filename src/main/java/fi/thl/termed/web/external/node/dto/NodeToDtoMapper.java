package fi.thl.termed.web.external.node.dto;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphDto;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.TypeId;

public class NodeToDtoMapper implements BiFunction<Node, NodeToDtoMapperConfig, NodeDto> {

  private static final String DEFAULT_BASE_URI = "http://termed.thl.fi/api";

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  private Function<GraphId, Graph> graphProvider;
  private Function<TypeId, Type> typeProvider;
  private BiFunction<Node, String, List<Node>> nodeReferenceProvider;
  private BiFunction<Node, String, List<Node>> nodeReferrerProvider;
  private String baseUri;

  public NodeToDtoMapper(
      Function<GraphId, Graph> graphProvider,
      Function<TypeId, Type> typeProvider,
      BiFunction<Node, String, List<Node>> nodeReferenceProvider,
      BiFunction<Node, String, List<Node>> nodeReferrerProvider,
      String baseUri) {
    this.graphProvider = graphProvider;
    this.typeProvider = typeProvider;
    this.nodeReferenceProvider = nodeReferenceProvider;
    this.nodeReferrerProvider = nodeReferrerProvider;
    this.baseUri = baseUri;
  }

  public NodeDto apply(Node node, NodeToDtoMapperConfig config) {
    return nodeToDto(node, new HashSet<>(), 0, 0, config);
  }

  private NodeDto nodeToDto(Node node, Set<NodeId> visited, int referenceDepth, int referrerDepth,
                            NodeToDtoMapperConfig config) {

    visited.add(node.identifier());

    NodeDto dto = new NodeDto();
    Type type = typeProvider.apply(node.getType());

    dto.setId(node.getId());
    dto.setUri(node.getUri());
    dto.setCode(node.getCode());

    dto.setType(typeDto(type, config));

    if (config.isLoadAudit()) {
      dto.setCreatedBy(node.getCreatedBy());
      dto.setCreatedDate(node.getCreatedDate());
      dto.setLastModifiedBy(node.getLastModifiedBy());
      dto.setLastModifiedDate(node.getLastModifiedDate());
    }

    dto.setProperties(mapProperties(node.getProperties(), type, config));

    if (referenceDepth < config.getMaxReferenceDepth()) {
      dto.setReferences(Multimaps.transformValues(
          filterAndLoadReferences(node, type, visited, config), ref ->
              nodeToDto(ref, visited, referenceDepth + 1, referrerDepth, config)));
    }

    if (referrerDepth < config.getMaxReferrerDepth()) {
      dto.setReferrers(Multimaps.transformValues(
          filterAndLoadReferrers(node, type, visited, config), ref ->
              nodeToDto(ref, visited, referenceDepth, referrerDepth + 1, config)));
    }

    return dto;
  }

  private Multimap<String, LangValue> mapProperties(Multimap<String, StrictLangValue> properties,
                                                    Type type, NodeToDtoMapperConfig config) {

    Multimap<String, LangValue> mappedProperties =
        Multimaps.transformValues(properties, LangValue::new);

    if (!config.getSelectProperty().isEmpty()) {
      mappedProperties = Multimaps.filterKeys(
          mappedProperties, key -> config.getSelectProperty().contains(key));
    }

    if (config.isUseUriKeys()) {
      Multimap<String, LangValue> uriMappedProperties = LinkedHashMultimap.create();
      for (Map.Entry<String, Collection<LangValue>> entry : mappedProperties.asMap().entrySet()) {
        uriMappedProperties.putAll(
            getTextAttributeUri(type, type.getTextAttribute(entry.getKey())), entry.getValue());
      }
      mappedProperties = uriMappedProperties;
    }

    return mappedProperties;
  }

  private Multimap<String, Node> filterAndLoadReferences(Node node, Type type, Set<NodeId> visited,
                                                         NodeToDtoMapperConfig config) {

    // remove keys that have all values visited
    Multimap<String, NodeId> unfiltered = node.getReferences();
    Multimap<String, NodeId> references =
        Multimaps.filterKeys(unfiltered, key -> !visited.containsAll(unfiltered.get(key)));

    if (!config.getSelectReference().isEmpty()) {
      references = Multimaps.filterKeys(references,
                                        key -> config.getSelectReference().contains(key));
    }

    Multimap<String, Node> loadedReferences = LinkedHashMultimap.create();
    for (String key : references.keySet()) {
      loadedReferences.putAll(key, nodeReferenceProvider.apply(node, key));
    }

    if (config.isUseUriKeys()) {
      Multimap<String, Node> uriMappedReferences = LinkedHashMultimap.create();
      for (String key : references.keySet()) {
        uriMappedReferences.putAll(
            getReferenceAttributeUri(type, type.getReferenceAttribute(key)),
            loadedReferences.get(key));
      }
      loadedReferences = uriMappedReferences;
    }

    return LinkedHashMultimap.create(
        Multimaps.filterValues(loadedReferences, value -> !visited.contains(value.identifier())));
  }

  private Multimap<String, Node> filterAndLoadReferrers(Node node, Type type, Set<NodeId> visited,
                                                        NodeToDtoMapperConfig config) {

    // remove keys that have all values visited
    Multimap<String, NodeId> unfiltered = node.getReferrers();
    Multimap<String, NodeId> referrers =
        Multimaps.filterKeys(unfiltered, key -> !visited.containsAll(unfiltered.get(key)));

    if (!config.getSelectReferrer().isEmpty()) {
      referrers = Multimaps.filterKeys(referrers, key -> config.getSelectReferrer().contains(key));
    }

    Multimap<String, Node> loadedReferrers = LinkedHashMultimap.create();
    for (String key : referrers.keySet()) {
      loadedReferrers.putAll(key, nodeReferrerProvider.apply(node, key));
    }

    if (config.isUseUriKeys()) {
      Multimap<String, Node> uriMappedReferrers = LinkedHashMultimap.create();
      for (String key : referrers.keySet()) {
        uriMappedReferrers.putAll(
            getReferenceAttributeUri(type, type.getReferenceAttribute(key)),
            loadedReferrers.get(key));
      }
      loadedReferrers = uriMappedReferrers;
    }

    return LinkedHashMultimap.create(
        Multimaps.filterValues(loadedReferrers, value -> !visited.contains(value.identifier())));
  }

  private String getTextAttributeUri(Type type, TextAttribute attribute) {
    if (!Strings.isNullOrEmpty(attribute.getUri())) {
      return attribute.getUri();
    }

    return String.format("%s/graphs/%s/types/%s/textAttributes/%s",
                         baseUri(), type.getGraphId(), type.getId(), attribute.getId());
  }

  private String getReferenceAttributeUri(Type type, ReferenceAttribute attribute) {
    if (!Strings.isNullOrEmpty(attribute.getUri())) {
      return attribute.getUri();
    }

    return String.format("%s/graphs/%s/types/%s/referenceAttributes/%s",
                         baseUri(), type.getGraphId(), type.getId(), attribute.getId());
  }

  private String baseUri() {
    return !baseUri.isEmpty() ? baseUri : DEFAULT_BASE_URI;
  }

  private TypeDto typeDto(Type type, NodeToDtoMapperConfig config) {
    TypeDto dto = new TypeDto();

    dto.setId(type.getId());
    dto.setGraph(graphDto(type.getGraph(), config));

    if (config.isLoadType()) {
      dto.setUri(type.getUri());
      dto.setProperties(type.getProperties());
    }

    return dto;
  }

  private GraphDto graphDto(GraphId graphId, NodeToDtoMapperConfig config) {
    GraphDto dto = new GraphDto();

    dto.setId(graphId.getId());

    if (config.isLoadGraph()) {
      Graph g = graphProvider.apply(graphId);
      dto.setCode(g.getCode());
      dto.setUri(g.getUri());
      dto.setProperties(g.getProperties());
    }

    return dto;
  }

}
