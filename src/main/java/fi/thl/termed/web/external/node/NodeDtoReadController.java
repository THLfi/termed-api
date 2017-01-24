package fi.thl.termed.web.external.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeDto;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeQuery;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.graph.specification.GraphByCode;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByCode;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.service.type.specification.TypesById;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.transform.NodeQueryParser;
import fi.thl.termed.web.external.node.transform.NodeToDtoMapper;

import static fi.thl.termed.service.node.specification.NodeQueryToSpecification.toSpecification;
import static org.assertj.core.util.Strings.isNullOrEmpty;

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

  private NodeDto toDto(Node node, NodeQuery query, User user) {
    return buildDtoMapper(user).apply(node, query);
  }

  private List<NodeDto> toDto(List<Node> nodes, NodeQuery query, User user) {
    return nodes.stream()
        .map(FunctionUtils.partialApplySecond(buildDtoMapper(user), query))
        .collect(Collectors.toList());
  }

  private NodeToDtoMapper buildDtoMapper(User user) {
    return new NodeToDtoMapper(
        FunctionUtils.memoize(graphId -> graphService.get(graphId, user).get()),
        FunctionUtils.memoize(typeId -> typeService.get(typeId, user).get()),
        new IndexedReferenceLoader(nodeService, user),
        new IndexedReferrerLoader(nodeService, user));
  }

  @GetMapping
  public List<NodeDto> searchNodes(
      @RequestParam(value = "graphId", required = false) UUID graphId,
      @RequestParam(value = "typeId", required = false) String typeId,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    List<Type> types;

    if (graphId != null && typeId != null) {
      types = typeService.get(new TypeId(typeId, new GraphId(graphId)), user)
          .map(Collections::singletonList)
          .orElseThrow(NotFoundException::new);
    } else if (graphId != null) {
      types = typeService.get(new TypesByGraphId(graphId), user);
    } else if (typeId != null) {
      types = typeService.get(new TypesById(typeId), user);
    } else {
      types = typeService.get(user);
    }

    NodeQuery query = NodeQueryParser.parse(params);
    OrSpecification<NodeId, Node> spec = new OrSpecification<>();
    types.forEach(type -> spec.or(toSpecification(type, query.where)));

    return toDto(nodeService.get(spec, query.sort, query.max, user), query, user);
  }

  @GetMapping("/{graphCode}")
  public List<NodeDto> searchNodesOfAnyTypeInGraph(
      @PathVariable("graphCode") String graphCode,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    OrSpecification<NodeId, Node> spec = new OrSpecification<>();
    typeService.get(new TypesByGraphId(graphId.getId()), user)
        .forEach(type -> spec.or(toSpecification(type, query.where)));

    return toDto(nodeService.get(spec, query.sort, query.max, user), query, user);
  }

  @GetMapping("/{graphCode}/{typeId}")
  public List<NodeDto> searchNodesByGraphCodeAndTypeId(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);
    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = toSpecification(type, query.where);

    return toDto(nodeService.get(spec, query.sort, query.max, user), query, user);
  }

  @GetMapping("/{graphCode}/{typeId}/{nodeCode}")
  public NodeDto getNodeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = new AndSpecification<>(
        new NodesByGraphId(graphId.getId()),
        new NodesByTypeId(typeId),
        new NodesByCode(nodeCode));

    Node node = nodeService.getFirst(spec, user).orElseThrow(NotFoundException::new);

    return toDto(node, query, user);
  }

  @GetMapping(path = "/{graphCode}/{typeId}", params = "uri")
  public NodeDto getNodeByGraphCodeAndTypeIdAndNodeUri(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("uri") String nodeUri,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId.getId()),
          new NodesByTypeId(typeId),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId.getId()),
          new NodesByTypeId(typeId),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getFirst(spec, user).orElseThrow(NotFoundException::new);

    return toDto(node, query, user);
  }

  @GetMapping(path = "/{graphCode}", params = "uri")
  public NodeDto getNodeByGraphCodeAndNodeUri(
      @PathVariable("graphCode") String graphCode,
      @RequestParam("uri") String nodeUri,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getFirstKey(new GraphByCode(graphCode), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId.getId()),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId.getId()),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getFirst(spec, user).orElseThrow(NotFoundException::new);

    return toDto(node, query, user);
  }

  @GetMapping(params = {"graphId", "uri"})
  public NodeDto getNodeByGraphIdAndNodeUri(
      @RequestParam("graphId") UUID graphId,
      @RequestParam("uri") String nodeUri,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = new AndSpecification<>(
          new NodesByGraphId(graphId),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getFirst(spec, user).orElseThrow(NotFoundException::new);

    return toDto(node, query, user);
  }

  @GetMapping(params = "uri")
  public List<NodeDto> getNodesByUri(
      @RequestParam(value = "typeId", required = false) String typeId,
      @RequestParam(value = "uri") String nodeUri,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    Specification<NodeId, Node> uriSpec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      uriSpec = new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length())));
    } else {
      uriSpec = new NodesByUri(nodeUri);
    }

    List<Type> types = isNullOrEmpty(typeId) ? typeService.get(user)
                                             : typeService.get(new TypesById(typeId), user);

    OrSpecification<NodeId, Node> spec = new OrSpecification<>();
    types.forEach(type -> spec.or(new AndSpecification<>(
        new NodesByGraphId(type.getGraphId()),
        new NodesByTypeId(type.getId()),
        uriSpec)));

    return toDto(nodeService.get(spec, user), query, user);
  }

}
