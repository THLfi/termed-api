package fi.thl.termed.web.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.graph.specification.GraphByCode;
import fi.thl.termed.service.node.specification.NodeSpecificationFactory;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesWithoutReferences;
import fi.thl.termed.service.node.specification.NodesWithoutReferrers;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.QueryModel;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyTextAttributeValuePrefix;
import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyType;
import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byType;
import static fi.thl.termed.util.StringUtils.tokenize;
import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

/**
 * Collection of methods that implement common queries for node service.
 */
@Component
public class NodeControllerReadService {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  public List<Node> searchNodesOfAnyType(QueryModel qm, User user) {
    Specification<NodeId, Node> spec;

    if (qm.getQuery().isEmpty()) {
      spec = byAnyType(typeService.getKeys(user));
    } else {
      List<TextAttributeId> textAttributeIds = typeService.get(user).stream()
          .flatMap(c -> c.getTextAttributeIds().stream()).collect(Collectors.toList());
      spec = byAnyTextAttributeValuePrefix(textAttributeIds, tokenize(qm.getQuery()));
    }

    return nodeService.get(new Query<>(spec, qm), user).getValues();
  }

  public List<Node> searchNodesOfAnyTypeInGraph(String graphCode, QueryModel qm, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    return searchNodesOfAnyTypeInGraph(graphId.getId(), qm, user);
  }

  public List<Node> searchNodesOfAnyTypeInGraph(UUID graphId, QueryModel qm, User user) {
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (qm.getQuery().isEmpty()) {
      spec = byAnyType(typeService.getKeys(new TypesByGraphId(graphId), user));
    } else {
      List<TextAttributeId> textAttributeIds = typeService.get(new TypesByGraphId(graphId), user)
          .stream().flatMap(c -> c.getTextAttributeIds().stream()).collect(Collectors.toList());
      spec = byAnyTextAttributeValuePrefix(textAttributeIds, tokenize(qm.getQuery()));
    }

    return nodeService.get(new Query<>(spec, qm), user).getValues();
  }

  public List<Node> searchNodesOfType(String graphCode, String typeId, QueryModel qm, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    return searchNodesOfType(graphId.getId(), typeId, qm, user);
  }

  public List<Node> searchNodesOfType(UUID graphId, String typeId, QueryModel qm, User user) {
    Type t = typeService.get(new TypeId(typeId, graphId), user).orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (qm.getQuery().isEmpty()) {
      spec = byType(t.identifier());
    } else {
      spec = byAnyTextAttributeValuePrefix(t.getTextAttributeIds(), tokenize(qm.getQuery()));
    }

    return nodeService.get(new Query<>(spec, qm), user).getValues();
  }

  public List<Node> searchReferenceTreeRootNodes(
      String graphCode, String typeId, String attributeId, QueryModel qm, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    return searchReferenceTreeRootNodes(graphId.getId(), typeId, attributeId, qm, user);
  }

  public List<Node> searchReferenceTreeRootNodes(
      UUID graphId, String typeId, String attributeId, QueryModel qm, User user) {
    Type t = typeService.get(new TypeId(typeId, graphId), user).orElseThrow(NotFoundException::new);

    AndSpecification<NodeId, Node> spec = new AndSpecification<>(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId),
        new NodesWithoutReferrers(attributeId));

    if (!qm.getQuery().isEmpty()) {
      spec.and(byAnyTextAttributeValuePrefix(t.getTextAttributeIds(), tokenize(qm.getQuery())));
    }

    return nodeService.get(new Query<>(spec, LUCENE), user).getValues();
  }

  public List<Node> searchReferrerTreeRootNodes(
      String graphCode, String typeId, String attributeId, QueryModel qm, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    return searchReferrerTreeRootNodes(graphId.getId(), typeId, attributeId, qm, user);
  }

  public List<Node> searchReferrerTreeRootNodes(
      UUID graphId, String typeId, String attributeId, QueryModel qm, User user) {
    Type t = typeService.get(new TypeId(typeId, graphId), user).orElseThrow(NotFoundException::new);

    AndSpecification<NodeId, Node> spec = new AndSpecification<>(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId),
        new NodesWithoutReferences(attributeId));

    if (!qm.getQuery().isEmpty()) {
      spec.and(byAnyTextAttributeValuePrefix(t.getTextAttributeIds(), tokenize(qm.getQuery())));
    }

    return nodeService.get(new Query<>(spec, LUCENE), user).getValues();
  }

  public Node getNodeById(UUID id, String typeId, UUID graphId, User user) {
    return nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);
  }

  public Node getNodeByCode(String code, String typeId, String graphCode, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    return nodeService.getFirst(
        NodeSpecificationFactory.byTypeAndCode(new TypeId(typeId, graphId), code), user)
        .orElseThrow(NotFoundException::new);
  }

  public Node getNodeByUri(String uri, String graphCode, User user) {
    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    if (uri.matches("^urn:uuid:" + RegularExpressions.UUID + "$")) {
      UUID id = UUIDs.fromString(uri.substring("urn:uuid:".length()));
      return nodeService.getFirst(NodeSpecificationFactory.byGraphAndId(graphId, id), user)
          .orElseThrow(NotFoundException::new);
    } else {
      return nodeService.getFirst(NodeSpecificationFactory.byGraphAndUri(graphId, uri), user)
          .orElseThrow(NotFoundException::new);
    }
  }

}
