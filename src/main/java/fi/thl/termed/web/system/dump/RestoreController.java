package fi.thl.termed.web.system.dump;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @PostJsonMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  public void restore(@RequestBody Dump dump, @AuthenticationPrincipal User user) {
    TransactionStatus tx = manager.getTransaction(new DefaultTransactionDefinition());

    try {
      graphService.save(dump.getGraphs(), user);
      typeService.save(dump.getTypes(), user);
      nodeService.save(dump.getNodes(), user);
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }

    manager.commit(tx);
  }

}
