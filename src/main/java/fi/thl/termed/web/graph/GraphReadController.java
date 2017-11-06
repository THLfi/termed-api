package fi.thl.termed.web.graph;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs")
public class GraphReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @GetJsonMapping
  public List<Graph> getGraph(@AuthenticationPrincipal User user) {
    return graphService.getValues(user);
  }

  @GetJsonMapping("/{graphId}")
  public Graph getGraph(@PathVariable("graphId") UUID graphId, @AuthenticationPrincipal User user) {
    return graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);
  }

}
