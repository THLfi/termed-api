package fi.thl.termed.web.dump;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/dump", "/api/restore"})
public class RestoreController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private PlatformTransactionManager manager;

  @Autowired
  private EventBus eventBus;

  @PostJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void restore(@RequestBody Dump dump,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {

    if (user.getAppRole() == AppRole.ADMIN || user.getAppRole() == AppRole.SUPERUSER) {
      TransactionStatus tx = manager.getTransaction(new DefaultTransactionDefinition());

      try {
        graphService.save(dump.getGraphs(), saveMode(mode), opts(sync), user);
        typeService.save(dump.getTypes(), saveMode(mode), opts(sync), user);
        nodeService.save(dump.getNodes(), saveMode(mode), opts(sync), user);
      } catch (RuntimeException | Error e) {
        manager.rollback(tx);
        throw e;
      } finally {
        eventBus.post(new InvalidateCachesEvent());
      }

      manager.commit(tx);
    }
  }

}
