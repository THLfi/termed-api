package fi.thl.termed.web.external.node.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.QueryModel;
import fi.thl.termed.web.node.NodeControllerReadService;

@RestController
@RequestMapping("/api/ext")
public class NodeDtoReadController {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private NodeControllerReadService nodeReadService;

  @Value("fi.thl.termed.baseUri:")
  private String baseUri;

  private NodeDto toDto(Node node, NodeToDtoMapperConfig config, User user) {
    return buildDtoMapper(user).apply(node, config);
  }

  private List<NodeDto> toDto(List<Node> nodes, NodeToDtoMapperConfig config, User user) {
    return nodes.stream()
        .map(FunctionUtils.partialApplySecond(buildDtoMapper(user), config))
        .collect(Collectors.toList());
  }

  private NodeToDtoMapper buildDtoMapper(User user) {
    return new NodeToDtoMapper(
        FunctionUtils.memoize(graphId -> graphService.get(graphId, user).get()),
        FunctionUtils.memoize(typeId -> typeService.get(typeId, user).get()),
        new IndexedReferenceLoader(nodeService, user),
        new IndexedReferrerLoader(nodeService, user),
        baseUri);
  }

  @GetMapping
  public List<NodeDto> searchNodesOfAnyType(
      @ModelAttribute QueryModel qm,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.searchNodesOfAnyType(qm, user), config, user);
  }

  @GetMapping("/{graphCode}")
  public List<NodeDto> searchNodesOfAnyTypeInGraph(
      @PathVariable("graphCode") String graphCode,
      @ModelAttribute QueryModel qm,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.searchNodesOfAnyTypeInGraph(graphCode, qm, user), config, user);
  }

  @GetMapping("/{graphCode}/{typeId}")
  public List<NodeDto> findNodesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @ModelAttribute QueryModel qm,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.searchNodesOfType(graphCode, typeId, qm, user), config, user);
  }

  @GetMapping(path = "/{graphCode}/{typeId}", params = {"referenceTree=true", "selectReference"})
  public List<NodeDto> findNodeReferenceTreesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("selectReference") String attributeId,
      @ModelAttribute QueryModel qm,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.searchReferenceTreeRootNodes(
        graphCode, typeId, attributeId, qm, user), config, user);
  }

  @GetMapping(path = "/{graphCode}/{typeId}", params = {"referrerTree=true", "selectReferrer"})
  public List<NodeDto> findNodeReferrerTreesByType(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("selectReferrer") String attributeId,
      @ModelAttribute QueryModel qm,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.searchReferrerTreeRootNodes(
        graphCode, typeId, attributeId, qm, user), config, user);
  }

  @GetMapping("/{graphCode}/{typeId}/{nodeCode}")
  public NodeDto getNodeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.getNodeByCode(nodeCode, typeId, graphCode, user), config, user);
  }

  @GetMapping(path = "/{graphCode}", params = "uri")
  public NodeDto getNodeByUri(
      @PathVariable("graphCode") String graphCode,
      @RequestParam("uri") String nodeUri,
      @ModelAttribute NodeToDtoMapperConfig config,
      @AuthenticationPrincipal User user) {
    return toDto(nodeReadService.getNodeByUri(nodeUri, graphCode, user), config, user);
  }

}
