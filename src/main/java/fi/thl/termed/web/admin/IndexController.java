package fi.thl.termed.web.admin;

import static fi.thl.termed.util.query.AndSpecification.and;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.Service;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IndexController {

  @Autowired
  private EventBus eventBus;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @DeleteMapping("/index")
  @ResponseStatus(NO_CONTENT)
  public void reindex(@AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      eventBus.post(new ReindexEvent<>(
          nodeService.keys(new Query<>(new MatchAll<>()), user)));
    } else {
      throw new AccessDeniedException("");
    }
  }

  @DeleteMapping("/graphs/{graphId}/index")
  @ResponseStatus(NO_CONTENT)
  public void reindexGraph(
      @PathVariable("graphId") UUID graphId,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      eventBus.post(new ReindexEvent<>(
          nodeService.keys(new Query<>(new NodesByGraphId(graphId)), user)));
    } else {
      throw new AccessDeniedException("");
    }
  }


  @DeleteMapping("/graphs/{graphId}/types/{id}/index")
  @ResponseStatus(NO_CONTENT)
  public void reindexType(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      eventBus.post(new ReindexEvent<>(nodeService.keys(
          new Query<>(and(new NodesByGraphId(graphId), new NodesByTypeId(id))), user)));
    } else {
      throw new AccessDeniedException("");
    }
  }

}
