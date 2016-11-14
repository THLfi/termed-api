package fi.thl.termed.service.node;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.service.node.internal.AttributeValueInitializingNodeService;
import fi.thl.termed.service.node.internal.IdInitializingNodeService;
import fi.thl.termed.service.node.internal.IndexedNodeService;
import fi.thl.termed.service.node.internal.JdbcNodeDao;
import fi.thl.termed.service.node.internal.JdbcNodeReferenceAttributeValueDao;
import fi.thl.termed.service.node.internal.JdbcNodeTextAttributeValueDao;
import fi.thl.termed.service.node.internal.ReadAuthorizedNodeService;
import fi.thl.termed.service.node.internal.NodeDocumentConverter;
import fi.thl.termed.service.node.internal.NodeRepository;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.index.lucene.JsonStringConverter;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.WriteLoggingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

@Configuration
public class NodeServiceConfiguration {

  @Autowired
  private DataSource dataSource;
  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private PermissionEvaluator<TypeId> classEvaluator;
  @Autowired
  private PermissionEvaluator<TextAttributeId> textAttributeEvaluator;
  @Autowired
  private PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator;

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

    service = new IndexedNodeService(
        service, new LuceneIndex<>(indexPath,
                                   new JsonStringConverter<>(NodeId.class),
                                   new NodeDocumentConverter(gson)));
    eventBus.register(service);

    // Although database backed repository is secured, lucene backed indexed service is not.
    // That's why we again filter any read requests.
    service = new ReadAuthorizedNodeService(
        service, classEvaluator, textAttributeEvaluator, referenceAttributeEvaluator);

    service = new WriteLoggingService<>(service, getClass().getPackage().getName() + ".Service");

    service = new AttributeValueInitializingNodeService(service);
    service = new IdInitializingNodeService(service);

    return service;
  }

  private AbstractRepository<NodeId, Node> nodeRepository() {
    return new NodeRepository(
        new AuthorizedDao<>(nodeSystemDao(), nodeEvaluator()),
        new AuthorizedDao<>(textAttributeValueSystemDao(), textAttributeValueEvaluator()),
        new AuthorizedDao<>(referenceAttributeValueSystemDao(),
                            referenceAttributeValueEvaluator()));
  }

  private PermissionEvaluator<NodeId> nodeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> classEvaluator.hasPermission(u, new TypeId(o), p));
  }

  private PermissionEvaluator<NodeAttributeValueId> textAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> textAttributeEvaluator.hasPermission(
            u, new TextAttributeId(new TypeId(o.getNodeId()), o.getAttributeId()), p));
  }

  private PermissionEvaluator<NodeAttributeValueId> referenceAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> referenceAttributeEvaluator.hasPermission(
            u, new ReferenceAttributeId(new TypeId(o.getNodeId()), o.getAttributeId()), p));
  }

  private SystemDao<NodeId, Node> nodeSystemDao() {
    return new CachedSystemDao<>(new JdbcNodeDao(dataSource));
  }

  private SystemDao<NodeAttributeValueId, StrictLangValue> textAttributeValueSystemDao() {
    return new CachedSystemDao<>(new JdbcNodeTextAttributeValueDao(dataSource));
  }

  private SystemDao<NodeAttributeValueId, NodeId> referenceAttributeValueSystemDao() {
    return new CachedSystemDao<>(new JdbcNodeReferenceAttributeValueDao(dataSource));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
                                         user.getAppRole() == AppRole.SUPERUSER;
  }

}
