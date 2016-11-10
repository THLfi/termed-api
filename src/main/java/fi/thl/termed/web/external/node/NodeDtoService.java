package fi.thl.termed.web.external.node;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphDto;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.service.Service;

@Component
public class NodeDtoService {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  /**
   * Depth limited node tree.
   */
  public NodeDto nodeDto(NodeId nodeId, User user, int maxDepth) {
    return nodeDto(nodeId, user, DtoMappingConfig.builder()
        .setPopulateTextAttributes()
        .setPopulateReferenceAttributes()
        .setPopulateReferrerAttributes()
        .setMaxDepth(maxDepth).build());
  }

  /**
   * Node Dto with reference values populated recursively via given attribute id.
   **/
  public NodeDto nodeReferenceTreeDto(NodeId nodeId, User user, String attribute) {
    log.debug("Building node reference tree with root of {}", nodeId);
    return nodeDto(nodeId, user, DtoMappingConfig.builder()
        .setMaxDepth(Integer.MAX_VALUE)
        .setPopulateTextAttributes()
        .setPopulateReferenceAttributes()
        .selectPopulateReferenceAttributes(attribute).build());
  }

  /**
   * Node Dto with referrer values populated recursively via given attribute id.
   **/
  public NodeDto nodeReferrerTreeDto(NodeId nodeId, User user, String attribute) {
    log.debug("Building node referrer tree with root of {}", nodeId);
    return nodeDto(nodeId, user, DtoMappingConfig.builder()
        .setMaxDepth(Integer.MAX_VALUE)
        .setPopulateTextAttributes()
        .setPopulateReferrerAttributes()
        .selectPopulateReferrerAttributes(attribute).build());
  }

  public NodeDto nodeDto(NodeId nodeId, User user, DtoMappingConfig config) {
    return nodeDto(nodeService.get(nodeId, user).get(), user, config);
  }

  public NodeDto nodeDto(Node node, User user, DtoMappingConfig config) {
    return nodeDto(node, user, new HashSet<>(), 0, config);
  }

  @SuppressWarnings("Convert2streamapi")
  private NodeDto nodeDto(Node node, User user, Set<NodeId> visited, int depth,
                          DtoMappingConfig config) {

    visited.add(node.identifier());

    NodeDto dto = new NodeDto();

    dto.setId(node.getId());
    dto.setUri(node.getUri());
    dto.setCode(node.getCode());

    dto.setType(typeDto(node.getType(), user, config.populateTypes, config.populateGraphs));

    if (config.populateAuditFields) {
      dto.setCreatedBy(node.getCreatedBy());
      dto.setCreatedDate(node.getCreatedDate());
      dto.setLastModifiedBy(node.getLastModifiedBy());
      dto.setLastModifiedDate(node.getLastModifiedDate());
    }

    if (config.populateTextAttributes) {
      Multimap<String, LangValue> properties = LinkedHashMultimap.create();
      for (Map.Entry<String, StrictLangValue> entry : node.getProperties().entries()) {
        if (config.selectTextAttributes.isEmpty() ||
            config.selectTextAttributes.contains(entry.getKey())) {
          properties.put(entry.getKey(), new LangValue(entry.getValue()));
        }
      }
      dto.setProperties(properties);
    }

    if (depth < config.maxDepth && config.populateReferenceAttributes) {
      Multimap<String, NodeDto> references = LinkedHashMultimap.create();
      for (String attribute : node.getReferences().keySet()) {
        if (config.selectReferenceAttributes.isEmpty() ||
            config.selectReferenceAttributes.contains(attribute)) {
          for (Node ref : new IndexedReferenceLoader(nodeService, user, attribute).apply(node)) {
            if (!visited.contains(ref.identifier())) {
              references.put(attribute, nodeDto(ref, user, visited, depth + 1, config));
            }
          }
        }
      }
      dto.setReferences(references);
    }

    if (depth < config.maxDepth && config.populateReferrerAttributes) {
      Multimap<String, NodeDto> referrers = LinkedHashMultimap.create();
      for (String attribute : node.getReferrers().keySet()) {
        if (config.selectReferrerAttributes.isEmpty() ||
            config.selectReferrerAttributes.contains(attribute)) {
          for (Node ref : new IndexedReferrerLoader(nodeService, user, attribute).apply(node)) {
            if (!visited.contains(ref.identifier())) {
              referrers.put(attribute, nodeDto(ref, user, visited, depth + 1, config));
            }
          }
        }
      }
      dto.setReferrers(referrers);
    }

    return dto;
  }

  private TypeDto typeDto(TypeId typeId, User user, boolean populateType, boolean populateGraph) {
    TypeDto dto = new TypeDto();

    dto.setId(typeId.getId());
    dto.setGraph(graphDto(typeId.getGraph(), user, populateGraph));

    if (populateType) {
      Type t = typeService.get(typeId, user).get();
      dto.setUri(t.getUri());
      dto.setProperties(t.getProperties());
      dto.setTextAttributeUriIndex(
          t.getTextAttributes().stream()
              .collect(Collectors.toMap(Attribute::getId, Attribute::getUri)));
      dto.setReferenceAttributeUriIndex(
          t.getReferenceAttributes().stream()
              .collect(Collectors.toMap(Attribute::getId, Attribute::getUri)));
    }

    return dto;
  }

  private GraphDto graphDto(GraphId graphId, User user, boolean populate) {
    GraphDto dto = new GraphDto();

    dto.setId(graphId.getId());

    if (populate) {
      Graph g = graphService.get(graphId, user).get();
      dto.setCode(g.getCode());
      dto.setUri(g.getUri());
      dto.setProperties(g.getProperties());
    }

    return dto;
  }

  public static class DtoMappingConfig {

    private int maxDepth;

    private boolean populateTypes;
    private boolean populateGraphs;
    private boolean populateAuditFields;

    private boolean populateTextAttributes;
    private boolean populateReferenceAttributes;
    private boolean populateReferrerAttributes;

    // empty means select all
    private Set<String> selectTextAttributes = new HashSet<>();
    private Set<String> selectReferenceAttributes = new HashSet<>();
    private Set<String> selectReferrerAttributes = new HashSet<>();

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {

      private int maxDepth;

      private boolean populateTypes;
      private boolean populateGraphs;
      private boolean populateAuditFields;

      private boolean populateTextAttributes;
      private boolean populateReferenceAttributes;
      private boolean populateReferrerAttributes;

      private Set<String> selectTextAttributes = new HashSet<>();
      private Set<String> selectReferenceAttributes = new HashSet<>();
      private Set<String> selectReferrerAttributes = new HashSet<>();

      public Builder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
      }

      public Builder setPopulateTypes() {
        this.populateTypes = true;
        return this;
      }

      public Builder setPopulateGraphs() {
        this.populateGraphs = true;
        return this;
      }

      public Builder setPopulateAuditFields() {
        this.populateAuditFields = true;
        return this;
      }

      public Builder setPopulateTextAttributes() {
        this.populateTextAttributes = true;
        return this;
      }

      public Builder setPopulateReferenceAttributes() {
        this.populateReferenceAttributes = true;
        return this;
      }

      public Builder setPopulateReferrerAttributes() {
        this.populateReferrerAttributes = true;
        return this;
      }

      public Builder selectPopulateTextAttributes(String populateTextAttribute) {
        Objects.nonNull(populateTextAttribute);
        this.selectTextAttributes.add(populateTextAttribute);
        return this;
      }

      public Builder setSelectTextAttributes(Set<String> selectTextAttributes) {
        Objects.nonNull(selectTextAttributes);
        this.selectTextAttributes = selectTextAttributes;
        return this;
      }

      public Builder selectPopulateReferenceAttributes(String populateReferenceAttribute) {
        Objects.nonNull(populateReferenceAttribute);
        this.selectReferenceAttributes.add(populateReferenceAttribute);
        return this;
      }

      public Builder setSelectReferenceAttributes(Set<String> selectReferenceAttributes) {
        Objects.nonNull(selectReferenceAttributes);
        this.selectReferenceAttributes = selectReferenceAttributes;
        return this;
      }

      public Builder selectPopulateReferrerAttributes(String populateReferrerAttribute) {
        Objects.nonNull(populateReferrerAttribute);
        this.selectReferrerAttributes.add(populateReferrerAttribute);
        return this;
      }

      public Builder setSelectReferrerAttributes(Set<String> selectReferrerAttributes) {
        Objects.nonNull(selectReferrerAttributes);
        this.selectReferrerAttributes = selectReferrerAttributes;
        return this;
      }

      public DtoMappingConfig build() {
        DtoMappingConfig dtoMappingConfig = new DtoMappingConfig();
        dtoMappingConfig.maxDepth = this.maxDepth;

        dtoMappingConfig.populateTypes = this.populateTypes;
        dtoMappingConfig.populateGraphs = this.populateGraphs;
        dtoMappingConfig.populateAuditFields = this.populateAuditFields;

        dtoMappingConfig.populateTextAttributes = this.populateTextAttributes;
        dtoMappingConfig.populateReferenceAttributes = this.populateReferenceAttributes;
        dtoMappingConfig.populateReferrerAttributes = this.populateReferrerAttributes;

        dtoMappingConfig.selectTextAttributes = this.selectTextAttributes;
        dtoMappingConfig.selectReferenceAttributes = this.selectReferenceAttributes;
        dtoMappingConfig.selectReferrerAttributes = this.selectReferrerAttributes;

        return dtoMappingConfig;
      }
    }
  }

}
