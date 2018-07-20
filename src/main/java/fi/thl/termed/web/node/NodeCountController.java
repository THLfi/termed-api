package fi.thl.termed.web.node;

import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
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
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetJsonMapping("/node-count")
  public long get(
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @AuthenticationPrincipal User user) {

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));

    return nodeService.count(specifyByQuery(graphs, types, types, where), user);
  }

  @GetJsonMapping("/graphs/{graphId}/node-count")
  public long get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) {

    if (!graphService.exists(new GraphId(graphId), user)) {
      throw new NotFoundException();
    }

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    List<Type> anyDomain = toListAndClose(
        typeService.values(new Query<>(new TypesByGraphId(graphId)), user));

    return nodeService.count(specifyByQuery(graphs, types, anyDomain, where), user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/node-count")
  public long get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @AuthenticationPrincipal User user) {

    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    return nodeService.count(specifyByQuery(graphs, types, domain, where), user);
  }

}
