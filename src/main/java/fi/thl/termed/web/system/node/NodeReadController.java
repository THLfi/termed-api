package fi.thl.termed.web.system.node;

import static fi.thl.termed.util.StringUtils.tokenize;
import static fi.thl.termed.util.collect.MapUtils.entry;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByPropertyPrefix;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeReadController {

  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;

  private Specification<NodeId, Node> toPrefixQuery(List<TextAttribute> attrs, String q) {
    OrSpecification<NodeId, Node> spec = new OrSpecification<>();
    tokenize(q).forEach(t -> attrs.forEach(a -> spec.or(new NodesByPropertyPrefix(a.getId(), t))));
    return spec;
  }

  @GetJsonMapping("/nodes")
  @SuppressWarnings("unchecked")
  public List<Node> get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    OrSpecification<NodeId, Node> spec = new OrSpecification<>();

    typeService.get(user).forEach(type -> {
      AndSpecification<NodeId, Node> typeSpec = new AndSpecification<>();
      typeSpec.and(new NodesByTypeId(type.getId()));
      typeSpec.and(new NodesByGraphId(type.getGraphId()));
      if (!bypassIndex && !query.isEmpty()) {
        typeSpec.and(toPrefixQuery(type.getTextAttributes(), query));
      }
      spec.or(typeSpec);
    });

    return nodeService.get(spec, user,
        entry("bypassIndex", bypassIndex),
        entry("sort", sort),
        entry("max", max));
  }

  @GetJsonMapping("/graphs/{graphId}/nodes")
  @SuppressWarnings("unchecked")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    OrSpecification<NodeId, Node> spec = new OrSpecification<>();

    typeService.get(new TypesByGraphId(graphId), user).forEach(type -> {
      AndSpecification<NodeId, Node> typeSpec = new AndSpecification<>();
      typeSpec.and(new NodesByTypeId(type.getId()));
      typeSpec.and(new NodesByGraphId(type.getGraphId()));
      if (!bypassIndex && !query.isEmpty()) {
        typeSpec.and(toPrefixQuery(type.getTextAttributes(), query));
      }
      spec.or(typeSpec);
    });

    return nodeService.get(spec, user,
        entry("bypassIndex", bypassIndex),
        entry("sort", sort),
        entry("max", max));
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes")
  @SuppressWarnings("unchecked")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    AndSpecification<NodeId, Node> spec = new AndSpecification<>();

    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    spec.and(new NodesByTypeId(type.getId()));
    spec.and(new NodesByGraphId(type.getGraphId()));
    if (!bypassIndex && !query.isEmpty()) {
      spec.and(toPrefixQuery(type.getTextAttributes(), query));
    }

    return nodeService.get(spec, user,
        entry("bypassIndex", bypassIndex),
        entry("sort", sort),
        entry("max", max));
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  public Node get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User user) {

    return nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);
  }

}
