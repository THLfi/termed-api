package fi.thl.termed.service.dump.internal;

import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
    return dump(graphService.getKeys(user).stream()
        .map(GraphId::getId)
        .collect(Collectors.toList()), user);
  }

  public Dump dump(List<UUID> graphIds, User user) {
    Dump dump = new Dump();

    List<Graph> graphs = new ArrayList<>();
    List<Type> types = new ArrayList<>();
    List<Node> nodes = new ArrayList<>();

    for (UUID graphId : graphIds) {
      graphs.add(graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new));
      types.addAll(typeService.get(new TypesByGraphId(graphId), user));
      nodes.addAll(nodeService.get(new NodesByGraphId(graphId), user));
    }

    dump.setGraphs(graphs);
    dump.setTypes(types);
    dump.setNodes(nodes);

    return dump;
  }

  public void restore(Dump dump, User user) {
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
