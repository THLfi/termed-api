package fi.thl.termed.web.external.node.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.graph.specification.GraphByCode;
import fi.thl.termed.service.node.specification.NodeSpecificationFactory;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesWithoutReferences;
import fi.thl.termed.service.node.specification.NodesWithoutReferrers;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.NodeDtoService;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

@RestController
@RequestMapping("/api/external/")
public class NodeDtoReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private NodeDtoService nodeDtoService;

  @GetMapping("/{graphCode}")
  public List<NodeDto> findNodesByGraph(
      @PathVariable("graphCode") String graphCode,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    return nodeService.getKeys(new NodesByGraphId(graphId.getId()), user).stream()
        .map(nodeId -> nodeDtoService.nodeDto(nodeId, user, 1))
        .collect(Collectors.toList());
  }

  @GetMapping("/{graphCode}/{typeId}")
  public List<NodeDto> findNodesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    return nodeService.getKeys(
        NodeSpecificationFactory.byType(new TypeId(typeId, graphId)), user).stream()
        .map(nodeId -> nodeDtoService.nodeDto(nodeId, user, 1))
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/{graphCode}/{typeId}",
      params = {"tree=true", "attribute"})
  public List<NodeDto> findNodeTreesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("attribute") String attributeId,
      @RequestParam(value = "depth", required = false, defaultValue = "1") int depth,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    List<NodeId> rootIds = nodeService.getKeys(
        new Query<>(new AndSpecification<>(
            new NodesByGraphId(graphId.getId()),
            new NodesByTypeId(typeId),
            new NodesWithoutReferrers(attributeId)), LUCENE), user).getValues();

    return rootIds.stream().map(nodeId -> nodeDtoService.nodeDto(nodeId, user, depth))
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/{graphCode}/{typeId}",
      params = {"tree=true", "attribute", "referrers=true"})
  public List<NodeDto> findNodeReferrerTreesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("attribute") String attributeId,
      @RequestParam(value = "depth", required = false, defaultValue = "1") int depth,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    List<NodeId> rootIds = nodeService.getKeys(
        new Query<>(new AndSpecification<>(
            new NodesByGraphId(graphId.getId()),
            new NodesByTypeId(typeId),
            new NodesWithoutReferences(attributeId)), LUCENE), user).getValues();

    return rootIds.stream().map(nodeId -> nodeDtoService.nodeDto(nodeId, user, depth))
        .collect(Collectors.toList());
  }

  @GetMapping("/{graphCode}/{typeId}/{nodeCode}")
  public NodeDto getNodeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    NodeId nodeId = nodeService.getFirstKey(
        NodeSpecificationFactory.byTypeAndCode(new TypeId(typeId, graphId), nodeCode), user)
        .orElseThrow(NotFoundException::new);

    return nodeDtoService.nodeDto(nodeId, user, 2);
  }

  @GetMapping(path = "/{graphCode}/{typeId}/{nodeCode}",
      params = {"tree=true", "attribute=true"})
  public NodeDto getNodeTreeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @RequestParam("attribute") String attributeId,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    NodeId nodeId = nodeService.getFirstKey(
        NodeSpecificationFactory.byTypeAndCode(new TypeId(typeId, graphId), nodeCode), user)
        .orElseThrow(NotFoundException::new);

    return nodeDtoService.nodeReferenceTreeDto(nodeId, user, attributeId);
  }

  @GetJsonMapping(path = "/{graphCode}/{typeId}/{nodeCode}",
      params = {"tree=true", "attribute=true", "referrers=true"})
  public NodeDto getNodeReferrerTreeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @RequestParam("attribute") String attributeId,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    NodeId nodeId = nodeService.getFirstKey(
        NodeSpecificationFactory.byTypeAndCode(new TypeId(typeId, graphId), nodeCode), user)
        .orElseThrow(NotFoundException::new);

    return nodeDtoService.nodeReferrerTreeDto(nodeId, user, attributeId);
  }

  @GetJsonMapping(path = "/{graphCode}", params = "nodeUri=true")
  public NodeDto getNodeByUri(
      @PathVariable("graphCode") String graphCode,
      @RequestParam("nodeUri") String nodeUri,
      @AuthenticationPrincipal User user) {

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    NodeId nodeId = nodeService.getFirstKey(
        NodeSpecificationFactory.byGraphAndUri(graphId, nodeUri), user)
        .orElseThrow(NotFoundException::new);

    return nodeDtoService.nodeDto(nodeId, user, 2);
  }

}
