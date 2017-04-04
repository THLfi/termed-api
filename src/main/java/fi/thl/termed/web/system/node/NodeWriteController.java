package fi.thl.termed.web.system.node;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    return nodeService.get(nodeId, currentUser).orElseThrow(NotFoundException::new);
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
    return nodeService.get(nodeId, currentUser).orElseThrow(NotFoundException::new);
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
    return nodeService.get(nodeId, currentUser).orElseThrow(NotFoundException::new);
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
    return nodeService.get(nodeId, currentUser).orElseThrow(NotFoundException::new);
  }

  @DeleteMapping("/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @RequestBody NodeId nodeId,
      @AuthenticationPrincipal User currentUser) {
    nodeService.delete(nodeId, currentUser);
  }

  @DeleteMapping(path = "/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdList(
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User currentUser) {
    nodeService.delete(nodeIds, currentUser);
  }

  @DeleteMapping("/graphs/{graphId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @AuthenticationPrincipal User currentUser) {
    nodeService.delete(nodeService.getKeys(new NodesByGraphId(graphId), currentUser), currentUser);
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @AuthenticationPrincipal User currentUser) {
    nodeService.delete(nodeService.getKeys(new AndSpecification<>(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId)), currentUser), currentUser);
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
