package fi.thl.termed.web.system.node;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
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
import fi.thl.termed.service.node.specification.NodeSpecificationParser;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.TypeBasedNodeSpecificationFilter;
import fi.thl.termed.service.node.util.IndexedReferenceLoader;
import fi.thl.termed.service.node.util.IndexedReferrerLoader;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private Pattern prop = compile("^(properties|props|p)\\.(" + CODE + ")$");
  private Pattern refDepth = compile("^(references|refs|r)\\.(" + CODE + ")(:([0-9]+))?$");
  private Pattern backRefDepth = compile("^(referrers|refrs)\\.(" + CODE + ")(:([0-9]+))?$");

  private NodeSpecificationParser specificationParser = new NodeSpecificationParser();

  @GetJsonMapping("/node-trees")
  public List<SimpleNodeTree> get(
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    List<Type> types = typeService.get(user);

    OrSpecification<NodeId, Node> filtered = new OrSpecification<>();
    String whereSpecification = String.join(" AND ", where);

    if (!whereSpecification.isEmpty()) {
      Specification<NodeId, Node> unfiltered = specificationParser.apply(whereSpecification);
      types.forEach(t -> filtered.or(new TypeBasedNodeSpecificationFilter(t).apply(unfiltered)));
    } else {
      types.forEach(t -> filtered.or(new AndSpecification<>(
          new NodesByGraphId(t.getGraphId()), new NodesByTypeId(t.getId()))));
    }

    return toTrees(
        nodeService.get(filtered, ImmutableMap.of("sort", sort, "max", max), user),
        parseSelect(select), user);
  }

  @GetJsonMapping("/graphs/{graphId}/node-trees")
  public List<SimpleNodeTree> get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.get(new TypesByGraphId(graphId), user);

    OrSpecification<NodeId, Node> filtered = new OrSpecification<>();
    String whereSpecification = String.join(" AND ", where);

    if (!whereSpecification.isEmpty()) {
      Specification<NodeId, Node> unfiltered = specificationParser.apply(whereSpecification);
      types.forEach(t -> filtered.or(new TypeBasedNodeSpecificationFilter(t).apply(unfiltered)));
    } else {
      types.forEach(t -> filtered.or(new AndSpecification<>(
          new NodesByGraphId(t.getGraphId()), new NodesByTypeId(t.getId()))));
    }

    return toTrees(
        nodeService.get(filtered, ImmutableMap.of("sort", sort, "max", max), user),
        parseSelect(select), user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-trees")
  public List<SimpleNodeTree> get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = "") List<String> select,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @RequestParam(value = "sort", defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") Integer max,
      @AuthenticationPrincipal User user) {

    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> filtered;
    String whereSpecification = String.join(" AND ", where);

    if (!whereSpecification.isEmpty()) {
      filtered = new TypeBasedNodeSpecificationFilter(type)
          .apply(specificationParser.apply(whereSpecification));
    } else {
      filtered = new AndSpecification<>(
          new NodesByGraphId(type.getGraphId()), new NodesByTypeId(type.getId()));
    }

    return toTrees(
        nodeService.get(filtered, ImmutableMap.of("sort", sort, "max", max), user),
        parseSelect(select), user);
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

    return toTree(node, parseSelect(select), user);
  }

  private List<SimpleNodeTree> toTrees(List<Node> nodes, Select select, User user) {
    return nodes.stream().map(node -> toTree(node, select, user)).collect(toList());
  }

  private SimpleNodeTree toTree(Node node, Select s, User user) {
    NodeTree tree = new LazyLoadingNodeTree(node,
        new IndexedReferenceLoader(nodeService, user),
        new IndexedReferrerLoader(nodeService, user));
    tree = new FilteredNodeTree(tree, s.props, s.refs.keySet(), s.backRefs.keySet());
    tree = new DepthLimitedNodeTree(tree, s.refs, s.backRefs);
    return new SimpleNodeTree(tree);
  }

  private Select parseSelect(List<String> selectClauses) {
    Select s = new Select();
    selectClauses.stream()
        .flatMap(c -> Arrays.stream(c.split(",")))
        .map(String::trim)
        .forEach(c -> {
          match(prop, c).ifPresent(m -> s.props.add(m.group(2)));
          match(refDepth, c).ifPresent(m -> s.refs.put(m.group(2), intOrOne(m.group(4))));
          match(backRefDepth, c).ifPresent(m -> s.backRefs.put(m.group(2), intOrOne(m.group(4))));
        });
    return s;
  }

  private Optional<MatchResult> match(Pattern p, String str) {
    Matcher m = p.matcher(str);
    return m.matches() ? Optional.of(m.toMatchResult()) : Optional.empty();
  }

  private Integer intOrOne(String s) {
    return s != null ? Integer.parseInt(s) : 1;
  }

  private class Select {

    Set<String> props = new HashSet<>();
    Map<String, Integer> refs = new HashMap<>();
    Map<String, Integer> backRefs = new HashMap<>();

  }

}
