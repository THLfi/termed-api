package fi.thl.termed.web.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Changeset;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeChangesetController {

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostJsonMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "changeset=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "true") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "true") boolean generateUris,
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    TypeId type = new TypeId(typeId, new GraphId(graphId));

    Stream<Node> saves = Stream.concat(
        changeset.getSave().stream()
            .map(node -> Objects.equals(node.getType(), type) ? node : Node.builder()
                .id(node.getId(), type)
                .copyOptionalsFrom(node)
                .build()),
        changeset.getPatch().stream()
            .map(patch -> {
              NodeId id = new NodeId(requireNonNull(patch.getId()), type);
              Node prev = nodeService.get(id, user).orElseThrow(NotFoundException::new);
              return merge(prev, patch);
            }));

    Stream<NodeId> deletes =
        changeset.getDelete().stream()
            .map(node -> new NodeId(node.getId(), type));

    nodeService.saveAndDelete(
        saves, deletes, saveMode(mode), opts(sync, generateCodes, generateUris), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "changeset=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "true") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "true") boolean generateUris,
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    Stream<Node> saves = Stream.concat(
        changeset.getSave().stream()
            .map(node -> Objects.equals(node.getType(), TypeId.of(node.getTypeId(), graphId))
                ? node
                : Node.builder()
                    .id(node.getId(), node.getTypeId(), graphId)
                    .copyOptionalsFrom(node)
                    .build()),
        changeset.getPatch().stream()
            .map(patch -> {
              NodeId id = new NodeId(requireNonNull(patch.getId()), patch.getTypeId(), graphId);
              Node prev = nodeService.get(id, user).orElseThrow(NotFoundException::new);
              return merge(prev, patch);
            }));

    Stream<NodeId> deletes =
        changeset.getDelete().stream()
            .map(node -> new NodeId(node.getId(), node.getTypeId(), graphId));

    nodeService.saveAndDelete(
        saves, deletes, saveMode(mode), opts(sync, generateCodes, generateUris), user);
  }

  @PostJsonMapping(path = "/nodes", params = "changeset=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSave(
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "true") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "true") boolean generateUris,
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    Stream<Node> saves = Stream.concat(
        changeset.getSave().stream(),
        changeset.getPatch().stream()
            .map(patch -> merge(
                nodeService.get(patch.identifier(), user).orElseThrow(NotFoundException::new),
                patch)));

    Stream<NodeId> deletes =
        changeset.getDelete().stream();

    nodeService.saveAndDelete(
        saves, deletes, saveMode(mode), opts(sync, generateCodes, generateUris), user);
  }

  private Node merge(Node node, Node patch) {
    Node.Builder nodeBuilder = Node.builderFromCopyOf(node);

    patch.getCode().ifPresent(nodeBuilder::code);
    patch.getUri().ifPresent(nodeBuilder::uri);
    patch.getProperties().forEach(nodeBuilder::addProperty);
    patch.getReferences().forEach(nodeBuilder::addReference);

    return nodeBuilder.build();
  }

}
