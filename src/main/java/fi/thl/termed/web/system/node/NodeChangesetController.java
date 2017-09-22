package fi.thl.termed.web.system.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.Optional.ofNullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    TypeId type = new TypeId(typeId, new GraphId(graphId));
    changeset.getDelete().forEach(id -> id.setType(type));
    changeset.getSave().forEach(node -> node.setType(type));
    changeset.getPatch().forEach(node -> node.setType(type));

    List<Node> saves = new ArrayList<>(changeset.getSave());
    changeset.getPatch().forEach(p -> saves.add(
        merge(nodeService.get(p.identifier(), user).orElseThrow(NotFoundException::new), p)));

    nodeService.deleteAndSave(changeset.getDelete(), saves, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/graphs/{graphId}/nodes", params = "changeset=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSaveOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    changeset.getDelete().forEach(id -> id.setType(new TypeId(id.getTypeId(), graphId)));
    changeset.getSave().forEach(node -> node.setType(new TypeId(node.getTypeId(), graphId)));
    changeset.getPatch().forEach(node -> node.setType(new TypeId(node.getTypeId(), graphId)));

    List<Node> saves = new ArrayList<>(changeset.getSave());
    changeset.getPatch().forEach(p -> saves.add(
        merge(nodeService.get(p.identifier(), user).orElseThrow(NotFoundException::new), p)));

    nodeService.deleteAndSave(changeset.getDelete(), saves, saveMode(mode), opts(sync), user);
  }

  @PostJsonMapping(path = "/nodes", params = "changeset=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void deleteAndSave(
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Changeset<NodeId, Node> changeset,
      @AuthenticationPrincipal User user) {

    List<Node> saves = new ArrayList<>(changeset.getSave());
    changeset.getPatch().forEach(p -> saves.add(
        merge(nodeService.get(p.identifier(), user).orElseThrow(NotFoundException::new), p)));

    nodeService.deleteAndSave(changeset.getDelete(), saves, saveMode(mode), opts(sync), user);
  }

  private Node merge(Node node, Node patch) {
    ofNullable(patch.getCode()).ifPresent(node::setCode);
    ofNullable(patch.getUri()).ifPresent(node::setUri);
    patch.getProperties().entries().forEach(e -> node.addProperty(e.getKey(), e.getValue()));
    patch.getReferences().entries().forEach(e -> node.addReference(e.getKey(), e.getValue()));
    return node;
  }

}
