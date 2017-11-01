package fi.thl.termed.web.external.node;

import static fi.thl.termed.service.node.specification.NodeQueryToSpecification.toSpecification;
import static fi.thl.termed.util.query.AndSpecification.and;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Strings.isNullOrEmpty;

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
import fi.thl.termed.service.node.specification.NodesLastModifiedSince;
import fi.thl.termed.service.node.specification.NodesWithoutReferences;
import fi.thl.termed.service.node.specification.NodesWithoutReferrers;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.service.type.specification.TypesById;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.transform.NodeQueryParser;
import fi.thl.termed.web.external.node.transform.NodeToDtoMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
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
        .collect(toList());
  }

  private NodeToDtoMapper buildDtoMapper(User user) {
    return new NodeToDtoMapper(
        FunctionUtils.memoize(graphId -> graphService.get(graphId, user).get()),
        FunctionUtils.memoize(typeId -> typeService.get(typeId, user).get()),
        new IndexedReferenceLoader(nodeService, user),
        new IndexedReferrerLoader(nodeService, user));
  }

  @GetMapping
  @SuppressWarnings("unchecked")
  public List<NodeDto> searchNodes(
      @RequestParam(value = "graphId", required = false) UUID graphId,
      @RequestParam(value = "typeId", required = false) String typeId,
      @RequestParam(value = "modifiedSince", required = false) String date,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    List<Type> types;

    if (graphId != null && typeId != null) {
      types = typeService.get(new TypeId(typeId, new GraphId(graphId)), user)
          .map(Collections::singletonList)
          .orElseThrow(NotFoundException::new);
    } else if (graphId != null) {
      types = typeService.getValueStream(new TypesByGraphId(graphId), user).collect(toList());
    } else if (typeId != null) {
      types = typeService.getValueStream(new TypesById(typeId), user).collect(toList());
    } else {
      types = typeService.getValueStream(user).collect(toList());
    }

    List<Specification<NodeId, Node>> orClauses = new ArrayList<>();

    for (Type type : types) {
      List<Specification<NodeId, Node>> andClauses = new ArrayList<>();
      andClauses.add(toSpecification(type, query.where));
      if (date != null) {
        andClauses.add(new NodesLastModifiedSince(new DateTime(date).toDate()));
      }
      orClauses.add(and(andClauses));
    }

    OrSpecification<NodeId, Node> spec = OrSpecification.or(orClauses);

    return toDto(nodeService.getValues(new Query<>(spec, query.sort, query.max), user),
        query, user);
  }

  @GetMapping("/{graphCode}")
  @SuppressWarnings("unchecked")
  public List<NodeDto> searchNodesOfAnyTypeInGraph(
      @PathVariable("graphCode") String graphCode,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);

    List<Specification<NodeId, Node>> orClauses = new ArrayList<>();
    typeService.getValueStream(new TypesByGraphId(graphId.getId()), user)
        .forEach(type -> orClauses.add(toSpecification(type, query.where)));

    OrSpecification<NodeId, Node> spec = OrSpecification.or(orClauses);

    return toDto(
        nodeService.getValueStream(new Query<>(spec, query.sort, query.max), user)
            .collect(toList()),
        query, user);
  }

  @GetMapping("/{graphCode}/{typeId}")
  @SuppressWarnings("unchecked")
  public List<NodeDto> searchNodesByGraphCodeAndTypeId(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);
    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = toSpecification(type, query.where);

    return toDto(
        nodeService.getValueStream(new Query<>(spec, query.sort, query.max), user)
            .collect(toList()),
        query, user);
  }

  @GetMapping(path = "/{graphCode}/{typeId}", params = {"referenceTree", "attributeId"})
  @SuppressWarnings("unchecked")
  public List<NodeDto> searchRootNodesByGraphCodeAndTypeId(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @RequestParam("referenceTree") boolean referenceTree,
      @RequestParam("attributeId") String attributeId,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);
    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = AndSpecification.and(
        toSpecification(type, query.where),
        !referenceTree
            ? new NodesWithoutReferences(attributeId)
            : new NodesWithoutReferrers(attributeId));

    return toDto(
        nodeService.getValueStream(new Query<>(spec, query.sort, query.max), user)
            .collect(toList()),
        query, user);
  }

  @GetMapping("/{graphCode}/{typeId}/{nodeCode}")
  public NodeDto getNodeByCode(
      @PathVariable("graphCode") String graphCode,
      @PathVariable("typeId") String typeId,
      @PathVariable("nodeCode") String nodeCode,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = AndSpecification.and(
        new NodesByGraphId(graphId.getId()),
        new NodesByTypeId(typeId),
        new NodesByCode(nodeCode));

    Node node = nodeService.getValueStream(spec, user).findFirst()
        .orElseThrow(NotFoundException::new);

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

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      spec = AndSpecification.and(
          new NodesByGraphId(graphId.getId()),
          new NodesByTypeId(typeId),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = AndSpecification.and(
          new NodesByGraphId(graphId.getId()),
          new NodesByTypeId(typeId),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getValueStream(spec, user).findFirst()
        .orElseThrow(NotFoundException::new);

    return toDto(node, query, user);
  }

  @GetMapping(path = "/{graphCode}", params = "uri")
  public NodeDto getNodeByGraphCodeAndNodeUri(
      @PathVariable("graphCode") String graphCode,
      @RequestParam("uri") String nodeUri,
      @RequestParam MultiValueMap<String, String> params,
      @AuthenticationPrincipal User user) {

    NodeQuery query = NodeQueryParser.parse(params);

    GraphId graphId = graphService.getKeyStream(new GraphByCode(graphCode), user)
        .findFirst().orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (nodeUri.matches(RegularExpressions.URN_UUID)) {
      spec = AndSpecification.and(
          new NodesByGraphId(graphId.getId()),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = AndSpecification.and(
          new NodesByGraphId(graphId.getId()),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getValueStream(spec, user).findFirst()
        .orElseThrow(NotFoundException::new);

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
      spec = AndSpecification.and(
          new NodesByGraphId(graphId),
          new NodeById(UUIDs.fromString(nodeUri.substring("urn:uuid:".length()))));
    } else {
      spec = AndSpecification.and(
          new NodesByGraphId(graphId),
          new NodesByUri(nodeUri));
    }

    Node node = nodeService.getValueStream(spec, user).findFirst()
        .orElseThrow(NotFoundException::new);

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

    List<Type> types = isNullOrEmpty(typeId) ? typeService.getValueStream(user).collect(toList())
        : typeService.getValueStream(new TypesById(typeId), user).collect(toList());

    List<Specification<NodeId, Node>> orClauses = new ArrayList<>();
    types.forEach(type -> orClauses.add(AndSpecification.and(
        new NodesByGraphId(type.getGraphId()),
        new NodesByTypeId(type.getId()),
        uriSpec)));

    OrSpecification<NodeId, Node> spec = OrSpecification.or(orClauses);

    return toDto(nodeService.getValueStream(spec, user).collect(toList()), query, user);
  }

}
