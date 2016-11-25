package fi.thl.termed.service.dump.internal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;

public class DumpService {

  private Service<GraphId, Graph> graphService;
  private Service<TypeId, Type> typeService;
  private Service<NodeId, Node> nodeService;

  private PlatformTransactionManager manager;
  private TransactionDefinition definition;

  public DumpService(Service<GraphId, Graph> graphService,
                     Service<TypeId, Type> typeService,
                     Service<NodeId, Node> nodeService,
                     PlatformTransactionManager manager) {
    this(graphService, typeService, nodeService, manager, new DefaultTransactionDefinition());
  }

  public DumpService(Service<GraphId, Graph> graphService,
                     Service<TypeId, Type> typeService,
                     Service<NodeId, Node> nodeService,
                     PlatformTransactionManager manager,
                     TransactionDefinition definition) {
    this.graphService = graphService;
    this.typeService = typeService;
    this.nodeService = nodeService;
    this.manager = manager;
    this.definition = definition;
  }

  public Dump dump(User user) {
    if (user.getAppRole() != AppRole.ADMIN &&
        user.getAppRole() != AppRole.SUPERUSER) {
      throw new AccessDeniedException("Access is denied");
    }

    TransactionStatus tx = manager.getTransaction(definition);
    Dump dump = new Dump();
    try {
      dump.setGraphs(graphService.get(user));
      dump.setTypes(typeService.get(user));
      dump.setNodes(nodeService.get(user));
    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }
    manager.commit(tx);
    return dump;
  }

  public void restore(Dump dump, User user) {
    if (user.getAppRole() != AppRole.ADMIN &&
        user.getAppRole() != AppRole.SUPERUSER) {
      throw new AccessDeniedException("Access is denied");
    }

    TransactionStatus tx = manager.getTransaction(definition);
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
