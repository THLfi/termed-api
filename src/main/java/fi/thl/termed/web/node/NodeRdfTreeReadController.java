package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Multimaps.transformValues;
import static fi.thl.termed.service.node.select.Selects.selectReferences;
import static fi.thl.termed.service.node.select.Selects.selectReferrers;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.service.node.util.UriResolvers.nodeUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.refAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.textAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.typeUriResolver;
import static fi.thl.termed.util.GraphUtils.collectNodes;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.lang.String.join;

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
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.select.Selects;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.node.util.NodeToTriples;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeRdfTreeReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetRdfMapping("/node-trees")
  public Model get(
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, types, where);
    Set<Select> selects = new LinkedHashSet<>();
    selects.add(new SelectId());
    selects.add(new SelectType());
    selects.addAll(Selects.parse(join(",", select)));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();

      trees.flatMap(tree -> collectNodes(tree, t -> copyOf(t.getReferences().values())).stream())
          .map(this::toNode)
          .flatMap(n -> toTriples(user).apply(n).stream())
          .forEach(t -> model.getGraph().add(t));

      return model;
    }
  }

  @GetRdfMapping("/graphs/{graphId}/node-trees")
  public Model get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    if (!graphService.exists(new GraphId(graphId), user)) {
      throw new NotFoundException();
    }

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    List<Type> anyDomain = toListAndClose(
        typeService.values(new Query<>(new TypesByGraphId(graphId)), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, anyDomain, where);
    Set<Select> selects = new LinkedHashSet<>();
    selects.add(new SelectId());
    selects.add(new SelectType());
    selects.addAll(Selects.parse(join(",", select)));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();

      trees.flatMap(tree -> collectNodes(tree, t -> copyOf(t.getReferences().values())).stream())
          .map(this::toNode)
          .flatMap(n -> toTriples(user).apply(n).stream())
          .forEach(t -> model.getGraph().add(t));

      return model;
    }
  }

  @GetRdfMapping("/graphs/{graphId}/types/{typeId}/node-trees")
  public Model get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, where);
    Set<Select> selects = new LinkedHashSet<>();
    selects.add(new SelectId());
    selects.add(new SelectType());
    selects.addAll(Selects.parse(join(",", select)));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();

      trees.flatMap(tree -> collectNodes(tree, t -> copyOf(t.getReferences().values())).stream())
          .map(this::toNode)
          .flatMap(n -> toTriples(user).apply(n).stream())
          .forEach(t -> model.getGraph().add(t));

      return model;
    }
  }

  @GetRdfMapping("/graphs/{graphId}/types/{typeId}/node-trees/{id}")
  public Model get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @AuthenticationPrincipal User user) {

    Node node = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Set<Select> selects = new LinkedHashSet<>();
    selects.add(new SelectId());
    selects.add(new SelectType());
    selects.addAll(Selects.parse(join(",", select)));

    NodeTree tree = toTree(node, selects, user);

    Model model = ModelFactory.createDefaultModel();

    collectNodes(tree, t -> copyOf(t.getReferences().values())).stream()
        .map(this::toNode)
        .flatMap(n -> toTriples(user).apply(n).stream())
        .forEach(t -> model.getGraph().add(t));

    return model;
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

  private Node toNode(NodeTree t) {
    Node n = new Node(t.getId());

    n.setType(t.getType());

    n.setUri(t.getUri());
    n.setCode(t.getCode());
    n.setNumber(t.getNumber());

    n.setCreatedBy(t.getCreatedBy());
    n.setCreatedDate(t.getCreatedDate());
    n.setLastModifiedBy(t.getLastModifiedBy());
    n.setLastModifiedDate(t.getLastModifiedDate());

    n.setProperties(t.getProperties());
    n.setReferences(transformValues(t.getReferences(),
        r -> new NodeId(r.getId(), r.getType())));

    return n;
  }

  private Function<Node, List<Triple>> toTriples(User user) {
    return new NodeToTriples(
        typeUriResolver(id -> typeService.get(id, user)),
        textAttrUriResolver(id -> typeService.get(id, user)),
        refAttrUriResolver(id -> typeService.get(id, user)),
        nodeUriResolver(id -> nodeService.get(id, user)));
  }

}
