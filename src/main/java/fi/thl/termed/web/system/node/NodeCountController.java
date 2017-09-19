package fi.thl.termed.web.system.node;

import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.OrSpecification;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeCountController {

  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;

  @GetJsonMapping("/node-count")
  public long get(
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @AuthenticationPrincipal User user) {

    List<Type> types = typeService.get(user).collect(toList());

    Specification<NodeId, Node> spec = types.stream()
        .map(domain -> specifyByQuery(types, domain, String.join(" AND ", where)))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return nodeService.count(spec, user);
  }

  @GetJsonMapping("/graphs/{graphId}/node-count")
  public long get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Type> types = typeService.get(user).collect(toList());

    Specification<NodeId, Node> spec = typeService.get(new TypesByGraphId(graphId), user)
        .map(domain -> specifyByQuery(types, domain, String.join(" AND ", where)))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    return nodeService.count(spec, user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-count")
  public long get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "where", defaultValue = "") List<String> where,
      @AuthenticationPrincipal User user) {

    List<Type> types = typeService.get(user).collect(toList());
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(types, domain, String.join(" AND ", where));

    return nodeService.count(spec, user);
  }

}
