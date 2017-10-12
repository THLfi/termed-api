package fi.thl.termed.service.node;

import static fi.thl.termed.util.Converter.newConverter;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.internal.AttributeValueInitializingNodeService;
import fi.thl.termed.service.node.internal.DocumentToNode;
import fi.thl.termed.service.node.internal.IdInitializingNodeService;
import fi.thl.termed.service.node.internal.IndexedNodeService;
import fi.thl.termed.service.node.internal.JdbcNodeSequenceDao;
import fi.thl.termed.service.node.internal.JdbcPostgresNodeDao;
import fi.thl.termed.service.node.internal.JdbcPostgresNodeReferenceAttributeValueDao;
import fi.thl.termed.service.node.internal.JdbcPostgresNodeTextAttributeValueDao;
import fi.thl.termed.service.node.internal.NodeRepository;
import fi.thl.termed.service.node.internal.NodeToDocument;
import fi.thl.termed.service.node.internal.NodeWriteEventPostingService;
import fi.thl.termed.service.node.internal.ReadAuthorizedNodeService;
import fi.thl.termed.service.node.internal.ExtIdsInitializingNodeService;
import fi.thl.termed.service.node.internal.TimestampingNodeService;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.JsonStringConverter;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.DaoSequenceService;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.SynchronizedSequenceService;
import fi.thl.termed.util.service.TransactionalSequenceService;
import fi.thl.termed.util.service.TransactionalService;
import fi.thl.termed.util.service.WriteLoggingService;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NodeServiceConfiguration {

  @Autowired
  private DataSource dataSource;
  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private PermissionEvaluator<TypeId> typeEvaluator;
  @Autowired
  private PermissionEvaluator<TextAttributeId> textAttributeEvaluator;
  @Autowired
  private PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator;

  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<GraphId, Graph> graphService;

  @Value("${fi.thl.termed.index:}")
  private String indexPath;
  @Autowired
  private Gson gson;

  @Autowired
  private EventBus eventBus;

  @Bean
  public Service<NodeId, Node> nodeService() {
    Service<NodeId, Node> service =
        new TransactionalService<>(nodeRepository(), transactionManager);

    Index<NodeId, Node> nodeIndex = new LuceneIndex<>(
        indexPath, new JsonStringConverter<>(NodeId.class),
        newConverter(new NodeToDocument(gson), new DocumentToNode(gson)));
    service = new IndexedNodeService(service, nodeIndex, gson);

    eventBus.register(service);

    // Although database backed repository is secured, lucene backed indexed service is not.
    // That's why we again filter any read requests.
    service = new ReadAuthorizedNodeService(
        service, typeEvaluator, textAttributeEvaluator, referenceAttributeEvaluator);

    service = new WriteLoggingService<>(service, getClass().getPackage().getName() + ".Service");
    service = new NodeWriteEventPostingService(service, eventBus);

    service = new TimestampingNodeService(service);
    service = new ExtIdsInitializingNodeService(service, nodeSequenceService(),
        typeService::get, graphService::get);
    service = new AttributeValueInitializingNodeService(service, typeService::get);
    service = new IdInitializingNodeService(service);

    return service;
  }

  private Service<NodeId, Node> nodeRepository() {
    return new NodeRepository(
        new AuthorizedDao<>(nodeSystemDao(), nodeEvaluator()),
        new AuthorizedDao<>(textAttributeValueSystemDao(), textAttributeValueEvaluator()),
        new AuthorizedDao<>(referenceAttributeValueSystemDao(),
            referenceAttributeValueEvaluator()));
  }

  private SequenceService<TypeId> nodeSequenceService() {
    SequenceService<TypeId> sequenceService =
        new DaoSequenceService<>(
            new AuthorizedDao<>(nodeSequenceSystemDao(), nodeSequenceEvaluator()));

    sequenceService = new TransactionalSequenceService<>(sequenceService, transactionManager);
    sequenceService = new SynchronizedSequenceService<>(sequenceService);

    return sequenceService;
  }

  private PermissionEvaluator<NodeId> nodeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), (u, o, p) -> typeEvaluator.hasPermission(u, o.getType(), p));
  }

  private PermissionEvaluator<TypeId> nodeSequenceEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), typeEvaluator);
  }

  private PermissionEvaluator<NodeAttributeValueId> textAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> textAttributeEvaluator.hasPermission(
            u, new TextAttributeId(o.getNodeId().getType(), o.getAttributeId()), p));
  }

  private PermissionEvaluator<NodeAttributeValueId> referenceAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> referenceAttributeEvaluator.hasPermission(
            u, new ReferenceAttributeId(o.getNodeId().getType(), o.getAttributeId()), p));
  }

  private SystemDao<NodeId, Node> nodeSystemDao() {
    return new JdbcPostgresNodeDao(dataSource);
  }

  private SystemDao<TypeId, Integer> nodeSequenceSystemDao() {
    return new JdbcNodeSequenceDao(dataSource);
  }

  private SystemDao<NodeAttributeValueId, StrictLangValue> textAttributeValueSystemDao() {
    return new JdbcPostgresNodeTextAttributeValueDao(dataSource);
  }

  private SystemDao<NodeAttributeValueId, NodeId> referenceAttributeValueSystemDao() {
    return new JdbcPostgresNodeReferenceAttributeValueDao(dataSource);
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
        user.getAppRole() == AppRole.SUPERUSER;
  }

}
