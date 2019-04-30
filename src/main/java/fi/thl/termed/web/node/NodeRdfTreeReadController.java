package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Multimaps.transformValues;
import static fi.thl.termed.service.node.select.Selects.parse;
import static fi.thl.termed.service.node.select.Selects.qualify;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.service.node.util.UriResolvers.nodeUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.refAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.textAttrUriResolver;
import static fi.thl.termed.service.node.util.UriResolvers.typeUriResolver;
import static fi.thl.termed.util.GraphUtils.collectNodes;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.Queries.matchAll;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;

import fi.thl.termed.domain.DepthLimitedNodeTree;
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
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.node.util.NodeToTriples;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Map;
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
  @Autowired
  private Map<String, String> defaultNamespacePrefixes;

  @GetRdfMapping("/node-trees")
  public Model get(
      @RequestParam(value = "select", defaultValue = EMPTY_LIST) List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, types, where);
    List<Select> selects = qualify(types, types, parse(select));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefixes(defaultNamespacePrefixes);

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

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    List<Type> domains = toImmutableListAndClose(
        typeService.values(Queries.query(TypesByGraphId.of(graphId)), user));

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domains, where);
    List<Select> selects = qualify(types, domains, parse(select));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefixes(defaultNamespacePrefixes);

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

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    Type domain = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, where);
    List<Select> selects = qualify(types, of(domain), parse(select));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec, sort, max), user)) {
      Stream<SimpleNodeTree> trees = toTrees(nodes, selects, user);

      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefixes(defaultNamespacePrefixes);

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

    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    Type domain = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = and(
        NodesByGraphId.of(graphId),
        NodesByTypeId.of(typeId),
        NodesById.of(id));
    List<Select> selects = qualify(types, of(domain), parse(select));

    try (Stream<Node> nodes = nodeService.values(new Query<>(selects, spec), user)) {
      Node node = nodes.findFirst().orElseThrow(NotFoundException::new);

      NodeTree tree = toTree(node, selects, user);

      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefixes(defaultNamespacePrefixes);

      collectNodes(tree, t -> copyOf(t.getReferences().values())).stream()
          .map(this::toNode)
          .flatMap(n -> toTriples(user).apply(n).stream())
          .forEach(t -> model.getGraph().add(t));

      return model;
    }
  }

  private Stream<SimpleNodeTree> toTrees(Stream<Node> nodes, List<Select> selects, User user) {
    return nodes.map(node -> toTree(node, selects, user));
  }

  private SimpleNodeTree toTree(Node node, List<Select> selects, User user) {
    NodeTree tree = new LazyLoadingNodeTree(node,
        new IndexedReferenceLoader(nodeService, user, selects),
        new IndexedReferrerLoader(nodeService, user, selects));

    return new SimpleNodeTree(
        new DepthLimitedNodeTree(tree,
            Selects.selectReferences(selects),
            Selects.selectReferrers(selects)));
  }

  private Node toNode(NodeTree t) {
    return Node.builder()
        .id(t.getId(), t.getType())
        .uri(t.getUri().orElse(null))
        .code(t.getCode().orElse(null))
        .number(t.getNumber())
        .createdBy(t.getCreatedBy())
        .createdDate(t.getCreatedDate())
        .lastModifiedBy(t.getLastModifiedBy())
        .lastModifiedDate(t.getLastModifiedDate())
        .properties(t.getProperties())
        .references(transformValues(t.getReferences(), r -> new NodeId(r.getId(), r.getType())))
        .build();
  }

  private Function<Node, List<Triple>> toTriples(User user) {
    return new NodeToTriples(
        typeUriResolver(id -> typeService.get(id, user)),
        textAttrUriResolver(id -> typeService.get(id, user)),
        refAttrUriResolver(id -> typeService.get(id, user)),
        nodeUriResolver(id -> nodeService.get(id, user)));
  }

}
