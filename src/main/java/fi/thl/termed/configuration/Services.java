package fi.thl.termed.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeAndResources;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.index.Index;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.LoggingService;
import fi.thl.termed.service.common.RepositoryService;
import fi.thl.termed.service.common.TransactionalService;
import fi.thl.termed.service.resource.AttributeResolvingResourceService;
import fi.thl.termed.service.resource.IdResolvingResourceService;
import fi.thl.termed.service.resource.IndexingResourceService;
import fi.thl.termed.service.resource.SchemeIdResolvingResourceService;
import fi.thl.termed.service.scheme.IndexingSchemeService;
import fi.thl.termed.service.scheme.ResolvingSchemeService;
import fi.thl.termed.service.scheme.SchemeAndResourcesService;
import fi.thl.termed.service.scheme.ValidatingSchemeService;
import fi.thl.termed.util.StrictLangValue;

/**
 * Configures Services.
 */
@Configuration
public class Services {

  @Bean
  public Service<String, User> userService(Repository<String, User> userRepository,
                                           PlatformTransactionManager transactionManager) {
    return new TransactionalService<String, User>(
        new RepositoryService<String, User>(userRepository), transactionManager);
  }

  @Bean
  public Service<String, Property> propertyService(
      Repository<String, Property> propertyRepository,
      PlatformTransactionManager transactionManager) {
    return new TransactionalService<String, Property>(
        new RepositoryService<String, Property>(propertyRepository), transactionManager);
  }

  @Bean
  public Service<UUID, Scheme> schemeService(
      Repository<UUID, Scheme> schemeRepository,
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      SystemDao<UUID, Scheme> schemeSystemDao,
      SystemDao<ResourceId, Resource> resourceSystemDao,
      PlatformTransactionManager transactionManager) {

    Service<UUID, Scheme> service = new RepositoryService<UUID, Scheme>(schemeRepository);

    service = new TransactionalService<UUID, Scheme>(service, transactionManager);
    service = new IndexingSchemeService(service, resourceRepository, resourceIndex,
                                        resourceSystemDao);
    service = new ValidatingSchemeService(service);
    service = new ResolvingSchemeService(service, schemeSystemDao);

    return service;
  }

  @Bean
  public Service<ResourceId, Resource> resourceService(
      Repository<ResourceId, Resource> resourceRepository,
      Index<ResourceId, Resource> resourceIndex,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator,
      SystemDao<UUID, Scheme> schemeSystemDao,
      SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao,
      SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao,
      SystemDao<ResourceId, Resource> resourceSystemDao,
      SystemDao<ResourceAttributeValueId, StrictLangValue> textAttributeValueSystemDao,
      SystemDao<ResourceAttributeValueId, ResourceId> referenceAttributeValueSystemDao,
      PlatformTransactionManager transactionManager) {

    Service<ResourceId, Resource> service =
        new RepositoryService<ResourceId, Resource>(resourceRepository);

    service = new TransactionalService<ResourceId, Resource>(service, transactionManager);
    service = new IndexingResourceService(
        service, resourceRepository, resourceIndex, classIdPermissionEvaluator,
        textAttributeIdPermissionEvaluator, referenceAttributeIdPermissionEvaluator,
        referenceAttributeValueSystemDao);
    service = new LoggingService<ResourceId, Resource>(service, Resource.class);
    service = new AttributeResolvingResourceService(
        service, textAttributeSystemDao, referenceAttributeSystemDao, resourceSystemDao);
    service = new IdResolvingResourceService(service, resourceSystemDao);
    service = new SchemeIdResolvingResourceService(service, schemeSystemDao);

    return service;
  }

  @Bean
  public Service<UUID, SchemeAndResources> schemeAndResourcesService(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService,
      Dao<ClassId, Class> classDao,
      PlatformTransactionManager transactionManager) {

    Service<UUID, SchemeAndResources> service =
        new SchemeAndResourcesService(resourceService, schemeService, classDao);

    service = new TransactionalService<UUID, SchemeAndResources>(service, transactionManager);
    service = new LoggingService<UUID, SchemeAndResources>(service, SchemeAndResources.class);

    return service;
  }

}
