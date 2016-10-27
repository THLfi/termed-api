package fi.thl.termed.service.resource;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.service.resource.internal.AttributeValueInitializingResourceService;
import fi.thl.termed.service.resource.internal.IdInitializingResourceService;
import fi.thl.termed.service.resource.internal.IndexedResourceService;
import fi.thl.termed.service.resource.internal.JdbcResourceDao;
import fi.thl.termed.service.resource.internal.JdbcResourceReferenceAttributeValueDao;
import fi.thl.termed.service.resource.internal.JdbcResourceTextAttributeValueDao;
import fi.thl.termed.service.resource.internal.ReadAuthorizedResourceService;
import fi.thl.termed.service.resource.internal.ResourceDocumentConverter;
import fi.thl.termed.service.resource.internal.ResourceRepository;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.index.lucene.JsonStringConverter;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.LoggingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

@Configuration
public class ResourceServiceConfiguration {

  @Autowired
  private DataSource dataSource;
  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private PermissionEvaluator<ClassId> classEvaluator;
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
  public Service<ResourceId, Resource> resourceService() {
    Service<ResourceId, Resource> service =
        new TransactionalService<>(resourceRepository(), transactionManager);

    service = new IndexedResourceService(
        service, new LuceneIndex<>(indexPath,
                                   new JsonStringConverter<>(ResourceId.class),
                                   new ResourceDocumentConverter(gson)));
    eventBus.register(service);

    // Although database backed repository is secured, lucene backed indexed service is not.
    // That's why we again filter any read requests.
    service = new ReadAuthorizedResourceService(
        service, classEvaluator, textAttributeEvaluator, referenceAttributeEvaluator);

    service = new LoggingService<>(service, getClass().getPackage().getName() + ".Service");

    service = new AttributeValueInitializingResourceService(service);
    service = new IdInitializingResourceService(service);

    return service;
  }

  private AbstractRepository<ResourceId, Resource> resourceRepository() {
    return new ResourceRepository(
        new AuthorizedDao<>(resourceSystemDao(), resourceEvaluator()),
        new AuthorizedDao<>(textAttributeValueSystemDao(), textAttributeValueEvaluator()),
        new AuthorizedDao<>(referenceAttributeValueSystemDao(),
                            referenceAttributeValueEvaluator()));
  }

  private PermissionEvaluator<ResourceId> resourceEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> classEvaluator.hasPermission(u, new ClassId(o), p));
  }

  private PermissionEvaluator<ResourceAttributeValueId> textAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> textAttributeEvaluator.hasPermission(
            u, new TextAttributeId(new ClassId(o.getResourceId()), o.getAttributeId()), p));
  }

  private PermissionEvaluator<ResourceAttributeValueId> referenceAttributeValueEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        (u, o, p) -> referenceAttributeEvaluator.hasPermission(
            u, new ReferenceAttributeId(new ClassId(o.getResourceId()), o.getAttributeId()), p));
  }

  private SystemDao<ResourceId, Resource> resourceSystemDao() {
    return new CachedSystemDao<>(new JdbcResourceDao(dataSource));
  }

  private SystemDao<ResourceAttributeValueId, StrictLangValue> textAttributeValueSystemDao() {
    return new CachedSystemDao<>(new JdbcResourceTextAttributeValueDao(dataSource));
  }

  private SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueSystemDao() {
    return new CachedSystemDao<>(new JdbcResourceReferenceAttributeValueDao(dataSource));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
                                         user.getAppRole() == AppRole.SUPERUSER;
  }

}
