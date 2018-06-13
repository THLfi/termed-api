package fi.thl.termed.web.graph;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/graphs")
public class GraphWriteController {

  @Autowired
  private Service2<GraphId, Graph> graphService;

  @PostJsonMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Graph save(@RequestBody Graph graph,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    return graphService.get(graphService.save(graph, saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(params = "returnIdOnly=true", produces = MediaType.TEXT_PLAIN_VALUE)
  public String saveAndReturnIdOnly(@RequestBody Graph graph,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    return graphService.get(graphService.save(graph, saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new).getId().toString();
  }

  @PostJsonMapping(params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody Stream<Graph> graphs,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User currentUser) {
    graphService.save(graphs, saveMode(mode), opts(sync), currentUser);
  }

  @PutJsonMapping(path = "/{graphId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Graph save(@PathVariable("graphId") UUID graphId,
      @RequestBody Graph graph,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    graph = Graph.builder().id(graphId).copyOptionalsFrom(graph).build();
    return graphService.get(graphService.save(graph, saveMode(mode), opts(sync), user), user)
        .orElseThrow(NotFoundException::new);
  }

  @DeleteMapping(path = "/{graphId}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    graphService.delete(new GraphId(graphId), opts(sync), user);
  }

}
