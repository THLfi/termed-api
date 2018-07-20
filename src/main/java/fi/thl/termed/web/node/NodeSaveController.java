package fi.thl.termed.web.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PatchJsonMapping;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.UUID;
import java.util.stream.Stream;
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
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Stream<Node> nodes,
      @AuthenticationPrincipal User user) {
    TypeId type = TypeId.of(typeId, graphId);

    Stream<Node> nodesWithTypes = nodes.map(node -> {
      node.setType(type);
      return node;
    });

    nodeService.save(nodesWithTypes, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(TypeId.of(typeId, graphId));
    NodeId nodeId = nodeService.save(node, saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void saveAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Stream<Node> nodes,
      @AuthenticationPrincipal User user) {

    Stream<Node> nodesWithTypes = nodes.map(node -> {
      node.setType(TypeId.of(node.getTypeId(), graphId));
      return node;
    });

    nodeService.save(nodesWithTypes, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(TypeId.of(node.getTypeId(), graphId));
    NodeId nodeId = nodeService.save(node, saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PostJsonMapping(path = "/nodes", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void saveAll(
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Stream<Node> nodes,
      @AuthenticationPrincipal User user) {
    nodeService.save(nodes, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/nodes", produces = {})
  public Node saveOne(
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    NodeId nodeId = nodeService.save(node, saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PutJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    node.setType(TypeId.of(typeId, graphId));
    node.setId(id);
    NodeId nodeId = nodeService.save(node, saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

  @PatchJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node patchOneById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {
    Node baseNode = nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    ofNullable(node.getCode()).ifPresent(baseNode::setCode);
    ofNullable(node.getUri()).ifPresent(baseNode::setUri);
    node.getProperties().entries().forEach(e -> baseNode.addProperty(e.getKey(), e.getValue()));
    node.getReferences().entries().forEach(e -> baseNode.addReference(e.getKey(), e.getValue()));

    NodeId nodeId = nodeService.save(baseNode, saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

}
