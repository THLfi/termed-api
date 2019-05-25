package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableSetAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.Collections.singleton;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.http.HttpPreconditions;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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

  @DeleteMapping("/graphs/{graphId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @AuthenticationPrincipal User user) {
    Query<NodeId, Node> nodesByGraphId = new Query<>(new NodesByGraphId(graphId));

    if (disconnect) {
      Set<NodeId> deleteIds = toImmutableSetAndClose(nodeService.keys(nodesByGraphId, user));
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteIds, user),
          deleteIds.stream(), UPDATE, opts(sync), user);
    } else {
      nodeService.delete(nodeService.keys(nodesByGraphId, user), opts(sync), user);
    }
  }

  @DeleteMapping(path = "/graphs/{graphId}/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdsOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    Set<NodeId> deleteIds = nodeIds.stream()
        .map(id -> new NodeId(id.getId(), id.getTypeId(), graphId))
        .collect(toImmutableSet());

    if (disconnect) {
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteIds, user),
          deleteIds.stream(),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteIds.stream(), opts(sync), user);
    }
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteAllOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @AuthenticationPrincipal User user) {
    Query<NodeId, Node> nodesByType = new Query<>(and(
        new NodesByGraphId(graphId),
        new NodesByTypeId(typeId)));

    if (disconnect) {
      Set<NodeId> deleteIds = toImmutableSetAndClose(nodeService.keys(nodesByType, user));
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteIds, user),
          deleteIds.stream(),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(nodeService.keys(nodesByType, user), opts(sync), user);
    }
  }

  @DeleteMapping(path = "/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIds(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    Set<NodeId> deleteIds = ImmutableSet.copyOf(nodeIds);
    if (disconnect) {
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteIds, user),
          deleteIds.stream(),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteIds.stream(), opts(sync), user);
    }
  }

  @DeleteMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIdsOfType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @RequestBody List<NodeId> nodeIds,
      @AuthenticationPrincipal User user) {
    Set<NodeId> deleteIds = nodeIds.stream()
        .map(id -> new NodeId(id.getId(), typeId, graphId))
        .collect(toImmutableSet());

    if (disconnect) {
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteIds, user),
          deleteIds.stream(),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteIds.stream(), opts(sync), user);
    }
  }

  @DeleteMapping("/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @RequestBody NodeId deleteId,
      @AuthenticationPrincipal User user) {
    if (disconnect) {
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteId, user),
          Stream.of(deleteId),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteId, opts(sync), user);
    }
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  @ResponseStatus(NO_CONTENT)
  public void deleteById(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @AuthenticationPrincipal User user) {
    NodeId deleteId = new NodeId(id, typeId, graphId);
    if (disconnect) {
      nodeService.saveAndDelete(
          collectRefsAndDisconnect(deleteId, user),
          Stream.of(deleteId),
          UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteId, opts(sync), user);
    }
  }

  private Stream<Node> collectRefsAndDisconnect(NodeId deletedNodeId, User user) {
    return collectRefsAndDisconnect(singleton(deletedNodeId), user);
  }

  private Stream<Node> collectRefsAndDisconnect(Set<NodeId> deletedNodeIds, User user) {
    return deletedNodeIds.stream()
        .map(nodeId -> HttpPreconditions.checkFound(nodeService.get(nodeId, user)))
        .flatMap(node -> node.getReferrers().values().stream())
        // skip referrer nodes that will be deleted
        .filter(referrerId -> !deletedNodeIds.contains(referrerId))
        .distinct()
        .map(referrerId -> HttpPreconditions.checkFound(nodeService.get(referrerId, user)))
        .map(referrer -> Node.builderFromCopyOf(referrer)
            .references(Multimaps.filterValues(
                referrer.getReferences(),
                referrerReferenceId -> !deletedNodeIds.contains(referrerReferenceId)))
            .build());
  }

}
