package fi.thl.termed.web.system.node;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeDeleteController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @DeleteMapping("/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody NodeId nodeId,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeId, opts(sync), user);
  }

  @DeleteMapping(path = "/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIds(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeIds, opts(sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeService.getKeys(new NodesByGraphId(graphId), user), opts(sync), user);
  }

  @DeleteMapping(path = "/graphs/{graphId}/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdsOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeIds.stream()
        .map(id -> new NodeId(id.getId(), id.getTypeId(), graphId))
        .collect(toList()), opts(sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteAllOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeService.getKeys(and(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId)), user), opts(sync), user);
  }

  @DeleteMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdsOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeIds.stream()
        .map(id -> new NodeId(id.getId(), typeId, graphId))
        .collect(toList()), opts(sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(new NodeId(id, typeId, graphId), opts(sync), user);
  }

}
