package fi.thl.termed.web.node;

import static fi.thl.termed.service.node.select.Selects.selectReferences;
import static fi.thl.termed.service.node.select.Selects.selectReferrers;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.query.OrSpecification.or;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.Gson;
import fi.thl.termed.domain.DepthLimitedNodeTree;
import fi.thl.termed.domain.FilteredNodeTree;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LazyLoadingNodeTree;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeTree;
import fi.thl.termed.domain.SimpleNodeTree;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.select.Selects;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.json.JsonStream;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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
  @Autowired
  private Gson gson;

  @GetJsonMapping("/node-trees")
  public void get(
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);

    Specification<NodeId, Node> spec = or(toListAndClose(types.stream()
        .map(domain -> specifyByQuery(graphs, types, domain, join(" AND ", where)))));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);
      response.setContentType(APPLICATION_JSON_UTF8_VALUE);
      response.setCharacterEncoding(UTF_8.toString());
      JsonStream.write(response.getOutputStream(), gson, trees, SimpleNodeTree.class);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/node-trees")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);

    Specification<NodeId, Node> spec = or(toListAndClose(
        typeService.getValueStream(new TypesByGraphId(graphId), user)
            .map(domain -> specifyByQuery(graphs, types, domain, join(" AND ", where)))));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);
      response.setContentType(APPLICATION_JSON_UTF8_VALUE);
      response.setCharacterEncoding(UTF_8.toString());
      JsonStream.write(response.getOutputStream(), gson, trees, SimpleNodeTree.class);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-trees")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, join(" AND ", where));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);
      response.setContentType(APPLICATION_JSON_UTF8_VALUE);
      response.setCharacterEncoding(UTF_8.toString());
      JsonStream.write(response.getOutputStream(), gson, trees, SimpleNodeTree.class);
    }
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-trees/{id}")
  public SimpleNodeTree get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @AuthenticationPrincipal User user) {

    Node node = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    return toTree(node, Selects.parse(join(",", select)), user);
  }

  private Stream<SimpleNodeTree> toTrees(Stream<Node> nodes, Set<Select> selects, User user) {
    return nodes.map(node -> toTree(node, selects, user));
  }

  private SimpleNodeTree toTree(Node node, Set<Select> selects, User user) {
    NodeTree tree = new LazyLoadingNodeTree(node,
        new IndexedReferenceLoader(nodeService, user, selects),
        new IndexedReferrerLoader(nodeService, user, selects));
    tree = new DepthLimitedNodeTree(tree, selectReferences(selects), selectReferrers(selects));
    tree = new FilteredNodeTree(tree, selects);
    return new SimpleNodeTree(tree);
  }

}
