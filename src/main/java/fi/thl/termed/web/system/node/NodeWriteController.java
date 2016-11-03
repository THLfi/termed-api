package fi.thl.termed.web.system.node;

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

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api")
public class NodeWriteController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User currentUser) {
    for (Node node : nodes) {
      node.setType(new TypeId(typeId, new GraphId(graphId)));
    }
    nodeService.save(nodes, currentUser);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node post(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestBody Node node,
      @AuthenticationPrincipal User currentUser) {
    node.setType(new TypeId(typeId, new GraphId(graphId)));
    NodeId nodeId = nodeService.save(node, currentUser);
    return nodeService.get(nodeId, currentUser).get();
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("graphId") UUID graphId,
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User currentUser) {
    for (Node node : nodes) {
      node.setType(new TypeId(node.getTypeId(), graphId));
    }
    nodeService.save(nodes, currentUser);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node post(
      @PathVariable("graphId") UUID graphId,
      @RequestBody Node node,
      @AuthenticationPrincipal User currentUser) {
    node.setType(new TypeId(node.getTypeId(), graphId));
    NodeId nodeId = nodeService.save(node, currentUser);
    return nodeService.get(nodeId, currentUser).get();
  }

  @PostJsonMapping(path = "/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @RequestBody List<Node> nodes,
      @AuthenticationPrincipal User currentUser) {
    nodeService.save(nodes, currentUser);
  }

  @PostJsonMapping(path = "/nodes", produces = {})
  public Node post(
      @RequestBody Node node,
      @AuthenticationPrincipal User currentUser) {
    NodeId nodeId = nodeService.save(node, currentUser);
    return nodeService.get(nodeId, currentUser).get();
  }

  @PutJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node put(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestBody Node node,
      @AuthenticationPrincipal User currentUser) {
    node.setType(new TypeId(typeId, new GraphId(graphId)));
    node.setId(id);
    NodeId nodeId = nodeService.save(node, currentUser);
    return nodeService.get(nodeId, currentUser).get();
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    nodeService.delete(new NodeId(id, typeId, graphId), currentUser);
  }


}
