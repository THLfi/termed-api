package fi.thl.termed.web.external.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeDto;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;

import static com.google.common.collect.Multimaps.transformEntries;
import static com.google.common.collect.Multimaps.transformValues;

@Component
public class NodeDtoService {

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
    return nodeDto(nodeId, user,
                   (subjectId, visited, depth, attributeId, valueId) ->
                       !visited.contains(valueId) && depth < maxDepth,
                   (subjectId, visited, depth, attributeId, valueId) ->
                       !visited.contains(valueId) && depth < maxDepth);
  }

  /**
   * Node Dto with reference values populated recursively via given attribute id.
   **/
  public NodeDto nodeReferenceTreeDto(NodeId nodeId, User user, String populateAttrId) {
    return nodeDto(nodeId, user,
                   (subjectId, visited, depth, attributeId, valueId) ->
                       !visited.contains(valueId) && attributeId.equals(populateAttrId),
                   (subjectId, visited, depth, attributeId, valueId) -> false);
  }

  /**
   * Node Dto with referrer values populated recursively via given attribute id.
   **/
  public NodeDto nodeReferrerTreeDto(NodeId nodeId, User user, String populateAttrId) {
    return nodeDto(nodeId, user,
                   (subjectId, visited, depth, attributeId, valueId) -> false,
                   (subjectId, visited, depth, attributeId, valueId) ->
                       !visited.contains(valueId) && attributeId.equals(populateAttrId));
  }

  private NodeDto nodeDto(NodeId nodeId, User user,
                          PopulateRefPredicate populateReference,
                          PopulateRefPredicate populateReferrer) {
    return nodeDto(nodeId, user, true, new HashSet<>(), 1, populateReference, populateReferrer);
  }

  private NodeDto nodeDto(NodeId nodeId, User user, boolean populate,
                          Set<NodeId> visited, int depth,
                          PopulateRefPredicate populateReference,
                          PopulateRefPredicate populateReferrer) {

    visited.add(nodeId);

    NodeDto dto = new NodeDto();

    dto.setId(nodeId.getId());
    dto.setType(typeDto(nodeId.getType(), user, populate));

    if (populate) {
      Node n = nodeService.get(nodeId, user).get();

      dto.setCode(n.getCode());
      dto.setUri(n.getUri());

      dto.setCreatedBy(n.getCreatedBy());
      dto.setCreatedDate(n.getCreatedDate());
      dto.setLastModifiedBy(n.getLastModifiedBy());
      dto.setLastModifiedDate(n.getLastModifiedDate());

      dto.setProperties(transformValues(n.getProperties(),
                                        v -> new LangValue(v.getLang(), v.getValue())));

      dto.setReferences(transformEntries(
          n.getReferences(),
          (key, value) -> nodeDto(value, user,
                                  populateReference.test(nodeId, visited, depth, key, value),
                                  visited, depth + 1,
                                  populateReference,
                                  populateReferrer)));

      dto.setReferrers(transformEntries(
          n.getReferrers(),
          (key, value) -> nodeDto(value, user,
                                  populateReferrer.test(nodeId, visited, depth, key, value),
                                  visited, depth + 1,
                                  populateReference,
                                  populateReferrer)));
    }

    return dto;
  }

  private TypeDto typeDto(TypeId typeId, User user, boolean populate) {
    TypeDto dto = new TypeDto();

    dto.setId(typeId.getId());
    dto.setGraph(graphDto(typeId.getGraph(), user, populate));

    if (populate) {
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

  /**
   * Predicate to decide whether to populate reference or referrer attribute value.
   */
  public interface PopulateRefPredicate {

    boolean test(NodeId subject, Set<NodeId> visited, int depth, String attribute, NodeId value);

  }

}
