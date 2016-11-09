package fi.thl.termed.web.system.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyTextAttributeValuePrefix;
import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byAnyType;
import static fi.thl.termed.service.node.specification.NodeSpecificationFactory.byType;
import static fi.thl.termed.util.StringUtils.split;
import static fi.thl.termed.util.specification.Query.Engine.LUCENE;
import static fi.thl.termed.util.specification.Query.Engine.SQL;

@RestController
@RequestMapping("/api")
public class NodeReadController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @GetJsonMapping("/nodes")
  public List<Node> get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    Specification<NodeId, Node> spec;

    if (query.isEmpty()) {
      spec = byAnyType(typeService.getKeys(user));
    } else {
      spec = byAnyTextAttributeValuePrefix(
          typeService.get(user).stream()
              .flatMap(cls -> cls.getTextAttributeIds().stream())
              .collect(Collectors.toList()),
          split(query, "\\s"));
    }

    return nodeService.get(new Query<>(spec, orderBy, max, bypassIndex ? SQL : LUCENE),
                           user).getValues();
  }

  @GetJsonMapping("/graphs/{graphId}/nodes")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    Specification<NodeId, Node> spec;

    if (query.isEmpty()) {
      spec = byAnyType(typeService.getKeys(new TypesByGraphId(graphId), user));
    } else {
      spec = byAnyTextAttributeValuePrefix(
          typeService.get(new TypesByGraphId(graphId), user).stream()
              .flatMap(cls -> cls.getTextAttributeIds().stream())
              .collect(Collectors.toList()),
          split(query, "\\s"));
    }

    return nodeService.get(new Query<>(spec, orderBy, max, bypassIndex ? SQL : LUCENE),
                           user).getValues();
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public List<Node> get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User user) {

    Type type = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec;

    if (query.isEmpty()) {
      spec = byType(type.identifier());
    } else {
      spec = byAnyTextAttributeValuePrefix(type.getTextAttributeIds(), split(query, "\\s"));
    }

    return nodeService.get(new Query<>(spec, orderBy, max, bypassIndex ? SQL : LUCENE),
                           user).getValues();
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
