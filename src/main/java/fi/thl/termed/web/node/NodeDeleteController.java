package fi.thl.termed.web.node;

import static com.google.common.collect.Multimaps.filterValues;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.transaction.TransactionUtils;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
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

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  @DeleteMapping("/graphs/{graphId}/nodes")
  @ResponseStatus(NO_CONTENT)
  public void deleteAllOfGraph(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @AuthenticationPrincipal User user) {
    Query<NodeId, Node> nodesByGraphId = new Query<>(new NodesByGraphId(graphId));

    if (disconnect) {
      List<NodeId> deleteIds = toListAndClose(nodeService.keys(nodesByGraphId, user));
      saveAndDelete(collectRefsAndDisconnect(deleteIds, user), deleteIds, UPDATE, opts(sync), user);
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
    List<NodeId> deleteIds = nodeIds.stream()
        .map(id -> new NodeId(id.getId(), id.getTypeId(), graphId))
        .collect(toList());

    if (disconnect) {
      saveAndDelete(collectRefsAndDisconnect(deleteIds, user), deleteIds, UPDATE, opts(sync), user);
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
      List<NodeId> deleteIds = toListAndClose(nodeService.keys(nodesByType, user));
      saveAndDelete(collectRefsAndDisconnect(deleteIds, user), deleteIds, UPDATE, opts(sync), user);
    } else {
      nodeService.delete(nodeService.keys(nodesByType, user), opts(sync), user);
    }
  }

  @DeleteMapping(path = "/nodes", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void deleteByIds(
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "disconnect", defaultValue = "false") boolean disconnect,
      @RequestBody List<NodeId> deleteIds,
      @AuthenticationPrincipal User user) {
    if (disconnect) {
      saveAndDelete(collectRefsAndDisconnect(deleteIds, user), deleteIds, UPDATE, opts(sync), user);
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
    List<NodeId> deleteIds = nodeIds.stream()
        .map(id -> new NodeId(id.getId(), typeId, graphId))
        .collect(toList());

    if (disconnect) {
      saveAndDelete(collectRefsAndDisconnect(deleteIds, user), deleteIds, UPDATE, opts(sync), user);
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
      saveAndDelete(collectRefsAndDisconnect(deleteId, user),
          singletonList(deleteId), UPDATE, opts(sync), user);
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
      saveAndDelete(collectRefsAndDisconnect(deleteId, user),
          singletonList(deleteId), UPDATE, opts(sync), user);
    } else {
      nodeService.delete(deleteId, opts(sync), user);
    }
  }

  private List<Node> collectRefsAndDisconnect(List<NodeId> deleteIds, User user) {
    return deleteIds.stream()
        .flatMap(deleteId -> collectRefsAndDisconnect(deleteId, user).stream())
        .collect(toList());
  }

  private List<Node> collectRefsAndDisconnect(NodeId deleteId, User user) {
    Node delete = nodeService.get(deleteId, user).orElseThrow(NotFoundException::new);

    return delete.getReferrers().values().stream().map(referrerId -> {
      Node referrer = nodeService.get(referrerId, user).orElseThrow(IllegalStateException::new);

      return Node.builderFromCopyOf(referrer)
          .references(filterValues(referrer.getReferences(),
              referrerReferenceId -> !Objects.equals(referrerReferenceId, deleteId)))
          .build();
    }).collect(toList());
  }

  private void saveAndDelete(List<Node> saves, List<NodeId> deletes,
      SaveMode mode, WriteOptions opts, User user) {
    TransactionUtils.runInTransaction(transactionManager, () -> {
      nodeService.save(saves.stream(), mode, opts, user);
      nodeService.delete(deletes.stream(), opts, user);
      return null;
    }, (error) -> {
      Stream<NodeId> reindex = Stream.concat(
          saves.stream().map(Node::identifier),
          deletes.stream());
      eventBus.post(new ReindexEvent<>(() -> reindex));
    });
  }

}
