package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.service.node.select.NodeSelects.parse;
import static fi.thl.termed.service.node.select.NodeSelects.qualify;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.Queries.matchAll;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonWriter;
import fi.thl.termed.domain.DepthLimitedNodeTree;
import fi.thl.termed.domain.FilteredNodeTree;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LazyLoadingNodeTree;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeTree;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.select.NodeSelects;
import fi.thl.termed.service.node.sort.NodeSorts;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.node.util.NodeTreeToJsonStream;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.json.JsonWriters;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Sort;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeTreeReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetJsonMapping("/node-trees")
  public void get(
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @RequestParam(value = "pretty", defaultValue = "false") boolean pretty,
      @AuthenticationPrincipal User user,
      HttpServletResponse resp) throws IOException {

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, types, where);
    List<Select> selects = qualify(types, types, parse(select));
    List<Sort> sorts = NodeSorts.parse(sort);

    resp.setContentType(APPLICATION_JSON_UTF8_VALUE);
    resp.setCharacterEncoding(UTF_8.toString());

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sorts, max), user);
        JsonWriter writer = JsonWriters.from(resp.getOutputStream(), pretty)) {
      NodeTreeToJsonStream.toJson(toTrees(nodes, selects, user).iterator(), writer);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/node-trees")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @RequestParam(value = "pretty", defaultValue = "false") boolean pretty,
      @AuthenticationPrincipal User user,
      HttpServletResponse resp) throws IOException {

    if (!graphService.exists(new GraphId(graphId), user)) {
      throw new NotFoundException();
    }

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    List<Type> domains = toImmutableListAndClose(
        typeService.values(Queries.query(TypesByGraphId.of(graphId)), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domains, where);
    List<Select> selects = qualify(types, domains, parse(select));
    List<Sort> sorts = NodeSorts.parse(sort);

    resp.setContentType(APPLICATION_JSON_UTF8_VALUE);
    resp.setCharacterEncoding(UTF_8.toString());

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sorts, max), user);
        JsonWriter writer = JsonWriters.from(resp.getOutputStream(), pretty)) {
      NodeTreeToJsonStream.toJson(toTrees(nodes, selects, user).iterator(), writer);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-trees")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @RequestParam(value = "pretty", defaultValue = "false") boolean pretty,
      @AuthenticationPrincipal User user,
      HttpServletResponse resp) throws IOException {

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    Type domain = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, where);
    List<Select> selects = qualify(types, of(domain), parse(select));
    List<Sort> sorts = NodeSorts.parse(sort);

    resp.setContentType(APPLICATION_JSON_UTF8_VALUE);
    resp.setCharacterEncoding(UTF_8.toString());

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sorts, max), user);
        JsonWriter writer = JsonWriters.from(resp.getOutputStream(), pretty)) {
      NodeTreeToJsonStream.toJson(toTrees(nodes, selects, user).iterator(), writer);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-trees/{id}")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "pretty", defaultValue = "false") boolean pretty,
      @AuthenticationPrincipal User user,
      HttpServletResponse resp) throws IOException {

    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    Type domain = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = and(
        NodesByGraphId.of(graphId),
        NodesByTypeId.of(typeId),
        NodesById.of(id));

    if (nodeService.count(spec, user) < 1) {
      throw new NotFoundException();
    }

    List<Select> selects = qualify(types, of(domain), parse(select));

    resp.setContentType(APPLICATION_JSON_UTF8_VALUE);
    resp.setCharacterEncoding(UTF_8.toString());

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec), user);
        JsonWriter writer = JsonWriters.from(resp.getOutputStream(), pretty)) {
      Node root = nodes.findFirst().orElseThrow(NotFoundException::new);
      NodeTreeToJsonStream.toJson(toTree(root, selects, user), writer);
    }
  }

  private Stream<NodeTree> toTrees(Stream<Node> nodes, List<Select> selects, User user) {
    return nodes.map(node -> toTree(node, selects, user));
  }

  private NodeTree toTree(Node node, List<Select> selects, User user) {
    NodeTree tree = new LazyLoadingNodeTree(node,
        new IndexedReferenceLoader(nodeService, user, selects),
        new IndexedReferrerLoader(nodeService, user, selects));

    tree = new DepthLimitedNodeTree(tree,
        NodeSelects.toReferenceSelectsWithDepths(selects),
        NodeSelects.toReferrerSelectsWithDepths(selects));

    return new FilteredNodeTree(tree, ImmutableSet.copyOf(selects));
  }

}
