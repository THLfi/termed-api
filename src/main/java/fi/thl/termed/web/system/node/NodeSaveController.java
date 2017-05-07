package fi.thl.termed.web.system.node;

import static com.google.common.collect.ImmutableMap.of;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.DeleteAndSave;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeSaveController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void saveAllOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User user) {
    TypeId type = new TypeId(typeId, new GraphId(graphId));
    nodes.forEach(node -> node.setType(type));
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "deleteAndSave=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveAllOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody DeleteAndSave<NodeId, Node> deleteAndSave,
      @AuthenticationPrincipal User user) {
    TypeId type = new TypeId(typeId, new GraphId(graphId));
    deleteAndSave.getDeletes().forEach(id -> id.setType(type));
    deleteAndSave.getSaves().forEach(node -> node.setType(type));
    nodeService.deleteAndSave(deleteAndSave.getDeletes(), deleteAndSave.getSaves(),
        of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(new TypeId(typeId, new GraphId(graphId)));
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void saveAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User user) {
    nodes.forEach(node -> node.setType(new TypeId(node.getTypeId(), graphId)));
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "deleteAndSave=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody DeleteAndSave<NodeId, Node> deleteAndSave,
      @AuthenticationPrincipal User user) {
    deleteAndSave.getDeletes().forEach(id -> id.setType(new TypeId(id.getTypeId(), graphId)));
    deleteAndSave.getSaves().forEach(node -> node.setType(new TypeId(node.getTypeId(), graphId)));
    nodeService.deleteAndSave(deleteAndSave.getDeletes(), deleteAndSave.getSaves(),
        of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(new TypeId(node.getTypeId(), graphId));
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(path = "/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void saveAll(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User user) {
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/nodes", params = "deleteAndSave=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveAll(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody DeleteAndSave<NodeId, Node> deleteAndSave,
      @AuthenticationPrincipal User user) {
    nodeService.deleteAndSave(deleteAndSave.getDeletes(), deleteAndSave.getSaves(),
        of("sync", sync), user);
  }

  @PostJsonMapping(path = "/nodes", produces = {})
  public Node saveOne(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PutJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(new TypeId(typeId, new GraphId(graphId)));
    node.setId(id);
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

}
