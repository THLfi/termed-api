package fi.thl.termed.web.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * GraphService published as a JSON/REST service.
 */
@RestController
@RequestMapping(value = "/api/graphs")
public class GraphWriteController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Graph save(@RequestBody Graph graph, @AuthenticationPrincipal User user) {
    return graphService.get(graphService.save(graph, user), user)
        .orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "returnIdOnly=true", produces = MediaType.TEXT_PLAIN_VALUE)
  public String saveAndReturnIdOnly(@RequestBody Graph graph,
                                    @AuthenticationPrincipal User user) {
    return graphService.get(graphService.save(graph, user), user)
        .orElseThrow(NotFoundException::new).getId().toString();
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody List<Graph> graphs, @AuthenticationPrincipal User currentUser) {
    graphService.save(graphs, currentUser);
  }

  @PutJsonMapping(path = "/{graphId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Graph save(@PathVariable("graphId") UUID graphId,
                    @RequestBody Graph graph,
                    @AuthenticationPrincipal User user) {
    graph.setId(graphId);
    return graphService.get(graphService.save(graph, user), user)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(path = "/{graphId}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("graphId") UUID graphId,
                     @AuthenticationPrincipal User user) {
    graphService.delete(new GraphId(graphId), user);
  }

}
