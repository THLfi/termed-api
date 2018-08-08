package fi.thl.termed.web.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
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
import java.util.Objects;
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

    Stream<Node> nodesWithTypes = nodes.map(node ->
        Objects.equals(node.getType(), type)
            ? node
            : Node.builder()
                .id(node.getId(), type)
                .copyOptionalsFrom(node)
                .build());

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
    TypeId type = TypeId.of(typeId, graphId);

    Node nodeWithType = Objects.equals(node.getType(), type)
        ? node
        : Node.builder()
            .id(node.getId(), type)
            .copyOptionalsFrom(node)
            .build();

    NodeId nodeId = nodeService.save(nodeWithType, saveMode(mode), opts(sync), user);
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

    Stream<Node> nodesWithTypes = nodes.map(node ->
        Objects.equals(node.getType(), TypeId.of(node.getTypeId(), graphId))
            ? node
            : Node.builder()
                .id(node.getId(), TypeId.of(node.getTypeId(), graphId))
                .copyOptionalsFrom(node)
                .build());

    nodeService.save(nodesWithTypes, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Node saveOneOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Node node,
      @AuthenticationPrincipal User user) {

    Node nodeWithType = Objects.equals(node.getType(), TypeId.of(node.getTypeId(), graphId))
        ? node
        : Node.builder()
            .id(node.getId(), TypeId.of(node.getTypeId(), graphId))
            .copyOptionalsFrom(node)
            .build();

    NodeId nodeId = nodeService.save(nodeWithType, saveMode(mode), opts(sync), user);
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

    Node nodeWithId = Node.builder()
        .id(id, typeId, graphId)
        .copyOptionalsFrom(node)
        .build();

    NodeId nodeId = nodeService.save(nodeWithId, saveMode(mode), opts(sync), user);
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

    Node.Builder baseNode = Node.builderFromCopyOf(
        nodeService.get(new NodeId(id, typeId, graphId), user).orElseThrow(NotFoundException::new));

    node.getCode().ifPresent(baseNode::code);
    node.getUri().ifPresent(baseNode::uri);
    node.getProperties().entries().forEach(e -> baseNode.addProperties(e.getKey(), e.getValue()));
    node.getReferences().entries().forEach(e -> baseNode.addReferences(e.getKey(), e.getValue()));

    NodeId nodeId = nodeService.save(baseNode.build(), saveMode(mode), opts(sync), user);
    return nodeService.get(nodeId, user).orElseThrow(NotFoundException::new);
  }

}
