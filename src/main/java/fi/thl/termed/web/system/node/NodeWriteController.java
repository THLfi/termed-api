package fi.thl.termed.web.system.node;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api")
@SuppressWarnings("unchecked")
public class NodeWriteController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User user) {
    TypeId type = new TypeId(typeId, new GraphId(graphId));
    nodes.forEach(node -> node.setType(type));
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node post(
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
  public void post(
      @PathVariable("graphId") UUID graphId,
      @RequestBody List<Node> nodes,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodes.forEach(node -> node.setType(new TypeId(node.getTypeId(), graphId)));
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node post(
      @PathVariable("graphId") UUID graphId,
      @RequestBody Node node,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    node.setType(new TypeId(node.getTypeId(), graphId));
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(path = "/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User user) {
    nodeService.save(nodes, of("sync", sync), user);
  }

  @PostJsonMapping(path = "/nodes", produces = {})
  public Node post(
      @RequestBody Node node,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    NodeId nodeId = nodeService.save(node, of("sync", sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PutJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node put(
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

  @DeleteMapping("/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @RequestBody NodeId nodeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeId, of("sync", sync), user);
  }

  @DeleteMapping(path = "/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdList(
      @RequestBody List<NodeId> nodeIds,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeIds, of("sync", sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeService.getKeys(new NodesByGraphId(graphId), user).collect(toList()),
        of("sync", sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(nodeService.getKeys(new AndSpecification<>(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId)), user).collect(toList()), of("sync", sync), user);
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    nodeService.delete(new NodeId(id, typeId, graphId), of("sync", sync), user);
  }

}
