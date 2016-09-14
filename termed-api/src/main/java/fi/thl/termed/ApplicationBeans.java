package fi.thl.termed;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.dao.jdbc.JdbcClassDao;
import fi.thl.termed.dao.jdbc.JdbcClassPermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcClassPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyDao;
import fi.thl.termed.dao.jdbc.JdbcPropertyPropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcReferenceAttributePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceDao;
import fi.thl.termed.dao.jdbc.JdbcResourceReferenceAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcResourceTextAttributeValueDao;
import fi.thl.termed.dao.jdbc.JdbcSchemeDao;
import fi.thl.termed.dao.jdbc.JdbcSchemePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcSchemePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcSchemeRoleDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributeDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributePermissionsDao;
import fi.thl.termed.dao.jdbc.JdbcTextAttributePropertyValueDao;
import fi.thl.termed.dao.jdbc.JdbcUserDao;
import fi.thl.termed.dao.jdbc.JdbcUserSchemeRoleDao;
import fi.thl.termed.dao.util.CachedSystemDao;
import fi.thl.termed.dao.util.SecureDao;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.domain.function.ObjectRolePermissionToObjectId;
import fi.thl.termed.domain.function.PropertyValueIdToSubjectId;
import fi.thl.termed.domain.function.ResourceAttributeValueIdToReferenceAttributeId;
import fi.thl.termed.domain.function.ResourceAttributeValueIdToTextAttributeId;
import fi.thl.termed.domain.function.ResourceIdToClassId;
import fi.thl.termed.domain.function.ResourceToClassId;
import fi.thl.termed.domain.function.SchemeRoleToSchemeId;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.exchange.Exporter;
import fi.thl.termed.exchange.rdf.ResourceRdfExchange;
import fi.thl.termed.exchange.table.ResourceTableExchange;
import fi.thl.termed.exchange.tree.ResourceContextJsTreeExporter;
import fi.thl.termed.exchange.tree.ResourceTreeExporter;
import fi.thl.termed.index.Index;
import fi.thl.termed.index.lucene.LuceneIndex;
import fi.thl.termed.index.lucene.ResourceDocumentConverter;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.common.AppRolePermissionEvaluator;
import fi.thl.termed.permission.common.DaoPermissionEvaluator;
import fi.thl.termed.permission.common.DisjunctionPermissionEvaluator;
import fi.thl.termed.permission.common.MappingPermissionEvaluator;
import fi.thl.termed.permission.common.PermitAllPermissionEvaluator;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.repository.impl.AbstractRepository;
import fi.thl.termed.repository.impl.ClassRepositoryImpl;
import fi.thl.termed.repository.impl.PropertyRepositoryImpl;
import fi.thl.termed.repository.impl.ReferenceAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.ResourceRepositoryImpl;
import fi.thl.termed.repository.impl.SchemeRepositoryImpl;
import fi.thl.termed.repository.impl.TextAttributeRepositoryImpl;
import fi.thl.termed.repository.impl.UserRepositoryImpl;
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
import fi.thl.termed.service.scheme.ValidatingSchemeService;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.util.DateTypeAdapter;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MultimapTypeAdapterFactory;
import fi.thl.termed.util.StrictLangValue;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.web.PropertyController;
import fi.thl.termed.web.ResourceContextJsTreeController;
import fi.thl.termed.web.ResourceControllerSpringImpl;
import fi.thl.termed.web.ResourceRdfController;
import fi.thl.termed.web.ResourceTableController;
import fi.thl.termed.web.ResourceTreeController;
import fi.thl.termed.web.SchemeController;
import fi.thl.termed.web.UserController;

@Configuration
public class ApplicationBeans {

  // Utils

  @Bean
  public Gson gson() {
    return new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(Date.class, new DateTypeAdapter().nullSafe())
        .registerTypeAdapterFactory(new MultimapTypeAdapterFactory())
        .create();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Controllers

  @Bean
  public ResourceControllerSpringImpl resourceController(
      Service<ResourceId, Resource> resourceService,
      Dao<UUID, Scheme> schemeDao,
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      PermissionEvaluator<UUID> schemePermissionEvaluator,
      PermissionEvaluator<ClassId> classPermissionEvaluator,
      PermissionEvaluator<TextAttributeId> textAttributeEvaluator) {
    return new ResourceControllerSpringImpl(resourceService,
                                            schemeDao,
                                            textAttributeDao,
                                            schemePermissionEvaluator,
                                            classPermissionEvaluator,
                                            textAttributeEvaluator);
  }

  @Bean
  public SchemeController schemeController(Service<UUID, Scheme> schemeService) {
    return new SchemeController(schemeService);
  }

  @Bean
  public ResourceTreeController resourceTreeController(
      Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter) {
    return new ResourceTreeController(resourceTreeExporter);
  }

  @Bean
  public ResourceContextJsTreeController resourceContextJsTreeController(
      Exporter<ResourceId, Resource, List<JsTree>> resourceContextJsTreeExporter) {
    return new ResourceContextJsTreeController(resourceContextJsTreeExporter);
  }

  @Bean
  public ResourceTableController resourceTableController(
      Exchange<ResourceId, Resource, List<String[]>> resourceTableExchange) {
    return new ResourceTableController(resourceTableExchange);
  }

  @Bean
  public ResourceRdfController resourceRdfController(
      Exchange<ResourceId, Resource, RdfModel> resourceRdfExchange) {
    return new ResourceRdfController(resourceRdfExchange);
  }

  @Bean
  public PropertyController propertyController(Service<String, Property> propertyService) {
    return new PropertyController(propertyService);
  }

  @Bean
  public UserController userController(Service<String, User> userService,
                                       PasswordEncoder passwordEncoder) {
    return new UserController(userService, passwordEncoder);
  }

  // Exporters

  @Bean
  public Exchange<ResourceId, Resource, List<String[]>> resourceTableExchange(
      Service<ResourceId, Resource> resourceService,
      Gson gson) {
    return new ResourceTableExchange(resourceService, gson);
  }

  @Bean
  public Exchange<ResourceId, Resource, RdfModel> resourceRdfExchange(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService) {
    return new ResourceRdfExchange(resourceService, schemeService);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<JsTree>> resourceContextJsTreeExporter(
      Service<ResourceId, Resource> resourceService,
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao) {
    return new ResourceContextJsTreeExporter(resourceService, referenceAttributeDao);
  }

  @Bean
  public Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter(
      Service<ResourceId, Resource> resourceService,
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao) {
    return new ResourceTreeExporter(resourceService, referenceAttributeDao);
  }

  // Services

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
        service, resourceRepository, resourceIndex, referenceAttributeValueSystemDao);
    service = new LoggingService<ResourceId, Resource>(service, Resource.class);
    service = new AttributeResolvingResourceService(
        service, textAttributeSystemDao, referenceAttributeSystemDao, resourceSystemDao);
    service = new IdResolvingResourceService(service, resourceSystemDao);
    service = new SchemeIdResolvingResourceService(service, schemeSystemDao);

    return service;
  }

  // Indices

  @Bean
  public Index<ResourceId, Resource> resourceIndex(
      @Value("${fi.thl.termed.index:}") String indexPath, Gson gson) {
    return new LuceneIndex<ResourceId, Resource>(indexPath, new ResourceDocumentConverter(gson));
  }

  // Repositories

  @Bean
  public Repository<String, User> userRepository(
      Dao<String, User> userDao,
      Dao<UserSchemeRoleId, Void> userSchemeRoleDao) {
    return new UserRepositoryImpl(userDao, userSchemeRoleDao);
  }

  @Bean
  public Repository<String, Property> propertyRepository(
      Dao<String, Property> propertyDao,
      Dao<PropertyValueId<String>, LangValue> propertyPropertyValueDao) {
    return new PropertyRepositoryImpl(propertyDao,
                                      propertyPropertyValueDao);
  }

  @Bean
  public Repository<UUID, Scheme> schemeRepository(
      Dao<UUID, Scheme> schemeDao,
      Dao<SchemeRole, Void> schemeRoleDao,
      Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao,
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      AbstractRepository<ClassId, Class> classRepository) {
    return new SchemeRepositoryImpl(schemeDao,
                                    schemeRoleDao,
                                    schemePermissionDao,
                                    schemePropertyValueDao,
                                    classRepository);
  }

  @Bean
  public AbstractRepository<ClassId, Class> classRepository(
      Dao<ClassId, Class> classDao,
      Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    return new ClassRepositoryImpl(classDao,
                                   classPermissionDao,
                                   classPropertyValueDao,
                                   textAttributeRepository,
                                   referenceAttributeRepository);
  }

  @Bean
  public AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao) {
    return new TextAttributeRepositoryImpl(textAttributeDao,
                                           textAttributePermissionDao,
                                           textAttributePropertyValueDao);
  }

  @Bean
  public AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao) {
    return new ReferenceAttributeRepositoryImpl(referenceAttributeDao,
                                                referenceAttributePermissionDao,
                                                referenceAttributePropertyValueDao);
  }

  @Bean
  public Repository<ResourceId, Resource> resourceRepository(
      Dao<ResourceId, Resource> resourceDao,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao,
      Dao<ResourceAttributeValueId, ResourceId> referenceAttributeValueDao,
      Dao<UUID, Scheme> schemeDao,
      Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
      Dao<ClassId, Class> classDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao) {
    return new ResourceRepositoryImpl(resourceDao,
                                      textAttributeValueDao,
                                      referenceAttributeValueDao,
                                      schemeDao,
                                      schemePropertyValueDao,
                                      classDao,
                                      classPropertyValueDao);
  }

  // Secured DAOs

  @Bean
  public Dao<String, User> userDao(SystemDao<String, User> userSystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<String, User>(userSystemDao, rolePermissions);
  }

  @Bean
  public Dao<UserSchemeRoleId, Void> userSchemeRoleDao(
      SystemDao<UserSchemeRoleId, Void> userSchemeRoleSystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<UserSchemeRoleId, Void>(userSchemeRoleSystemDao, rolePermissions);
  }

  @Bean
  public Dao<String, Property> propertyDao(SystemDao<String, Property> propertySystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.USER, Permission.READ)
        .putAll(AppRole.ADMIN, Permission.READ)
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<String, Property>(propertySystemDao, rolePermissions);
  }

  @Bean
  public Dao<PropertyValueId<String>, LangValue> propertyPropertyValueDao(
      SystemDao<PropertyValueId<String>, LangValue> propertyPropertySystemDao) {
    Multimap<AppRole, Permission> rolePermissions = ImmutableMultimap.<AppRole, Permission>builder()
        .putAll(AppRole.USER, Permission.READ)
        .putAll(AppRole.ADMIN, Permission.READ)
        .putAll(AppRole.SUPERUSER, Permission.values()).build();
    return new SecureDao<PropertyValueId<String>, LangValue>(propertyPropertySystemDao,
                                                             rolePermissions);
  }

  @Bean
  public Dao<UUID, Scheme> schemeDao(SystemDao<UUID, Scheme> schemeSystemDao,
                                     PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {
    return new SecureDao<UUID, Scheme>(schemeSystemDao, schemeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<SchemeRole, Void> schemeRoleDao(SystemDao<SchemeRole, Void> schemeRoleSystemDao,
                                             PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<SchemeRole> evaluator =
        new MappingPermissionEvaluator<SchemeRole, UUID>(
            new SchemeRoleToSchemeId(), schemeIdPermissionEvaluator);

    return new SecureDao<SchemeRole, Void>(schemeRoleSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao(
      SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionSystemDao,
      PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<UUID>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<UUID>, UUID>(
            new ObjectRolePermissionToObjectId<UUID>(), schemeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<UUID>, Void>(
        schemePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao(
      SystemDao<PropertyValueId<UUID>, LangValue> schemePropertyValueSystemDao,
      PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<UUID>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<UUID>, UUID>(
            new PropertyValueIdToSubjectId<UUID>(), schemeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<UUID>, LangValue>(
        schemePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ClassId, Class> classDao(SystemDao<ClassId, Class> classSystemDao,
                                      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {
    return new SecureDao<ClassId, Class>(classSystemDao, classIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao(
      SystemDao<ObjectRolePermission<ClassId>, Void> classPermissionSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<ClassId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<ClassId>, ClassId>(
            new ObjectRolePermissionToObjectId<ClassId>(), classIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<ClassId>, Void>(
        classPermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao(
      SystemDao<PropertyValueId<ClassId>, LangValue> classPropertyValueSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<ClassId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<ClassId>, ClassId>(
            new PropertyValueIdToSubjectId<ClassId>(), classIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<ClassId>, LangValue>(
        classPropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao(
      SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {
    return new SecureDao<ReferenceAttributeId, ReferenceAttribute>(
        referenceAttributeSystemDao, referenceAttributeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionDao(
      SystemDao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<ReferenceAttributeId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<ReferenceAttributeId>, ReferenceAttributeId>(
            new ObjectRolePermissionToObjectId<ReferenceAttributeId>(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<ReferenceAttributeId>, Void>(
        referenceAttributePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueDao(
      SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<ReferenceAttributeId>, ReferenceAttributeId>(
            new PropertyValueIdToSubjectId<ReferenceAttributeId>(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<ReferenceAttributeId>, LangValue>(
        referenceAttributePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<TextAttributeId, TextAttribute> textAttributeDao(
      SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {
    return new SecureDao<TextAttributeId, TextAttribute>(
        textAttributeSystemDao, textAttributeIdPermissionEvaluator, true);
  }

  @Bean
  public Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao(
      SystemDao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ObjectRolePermission<TextAttributeId>> evaluator =
        new MappingPermissionEvaluator<ObjectRolePermission<TextAttributeId>, TextAttributeId>(
            new ObjectRolePermissionToObjectId<TextAttributeId>(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<ObjectRolePermission<TextAttributeId>, Void>(
        textAttributePermissionSystemDao, evaluator, true);
  }

  @Bean
  public Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao(
      SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<PropertyValueId<TextAttributeId>> evaluator =
        new MappingPermissionEvaluator<PropertyValueId<TextAttributeId>, TextAttributeId>(
            new PropertyValueIdToSubjectId<TextAttributeId>(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<PropertyValueId<TextAttributeId>, LangValue>(
        textAttributePropertyValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ResourceId, Resource> resourceDao(
      SystemDao<ResourceId, Resource> resourceSystemDao,
      PermissionEvaluator<ClassId> classIdPermissionEvaluator,
      PermissionEvaluator<Specification<ResourceId, Resource>> resourceSpecificationPermissionEvaluator) {

    PermissionEvaluator<ResourceId> keyEvaluator =
        new MappingPermissionEvaluator<ResourceId, ClassId>(
            new ResourceIdToClassId(), classIdPermissionEvaluator);
    PermissionEvaluator<Resource> valEvaluator =
        new MappingPermissionEvaluator<Resource, ClassId>(
            new ResourceToClassId(), classIdPermissionEvaluator);

    return new SecureDao<ResourceId, Resource>(
        resourceSystemDao, keyEvaluator, valEvaluator,
        resourceSpecificationPermissionEvaluator, true);
  }

  @Bean
  public Dao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueDao(
      SystemDao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueSystemDao,
      PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ResourceAttributeValueId> evaluator =
        new MappingPermissionEvaluator<ResourceAttributeValueId, ReferenceAttributeId>(
            new ResourceAttributeValueIdToReferenceAttributeId(),
            referenceAttributeIdPermissionEvaluator);

    return new SecureDao<ResourceAttributeValueId, ResourceId>(
        resourceReferenceAttributeValueSystemDao, evaluator, true);
  }

  @Bean
  public Dao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueDao(
      SystemDao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueSystemDao,
      PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator) {

    PermissionEvaluator<ResourceAttributeValueId> evaluator =
        new MappingPermissionEvaluator<ResourceAttributeValueId, TextAttributeId>(
            new ResourceAttributeValueIdToTextAttributeId(),
            textAttributeIdPermissionEvaluator);

    return new SecureDao<ResourceAttributeValueId, StrictLangValue>(
        resourceTextAttributeValueSystemDao, evaluator, true);
  }

  // Permission evaluators

  @Bean
  public PermissionEvaluator<UUID> schemeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionSystemDao) {

    PermissionEvaluator<UUID> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<UUID>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<UUID> schemeIdPermissionEvaluator =
        new DaoPermissionEvaluator<UUID>(schemePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<UUID>(
        appRolePermissionEvaluator, schemeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<ClassId> classIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<ClassId>, Void> classPermissionSystemDao) {

    PermissionEvaluator<ClassId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<ClassId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<ClassId> classIdPermissionEvaluator =
        new DaoPermissionEvaluator<ClassId>(classPermissionSystemDao);

    return new DisjunctionPermissionEvaluator<ClassId>(
        appRolePermissionEvaluator, classIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionSystemDao) {

    PermissionEvaluator<TextAttributeId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<TextAttributeId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<TextAttributeId> textAttributeIdPermissionEvaluator =
        new DaoPermissionEvaluator<TextAttributeId>(textAttributePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<TextAttributeId>(
        appRolePermissionEvaluator, textAttributeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionSystemDao) {

    PermissionEvaluator<ReferenceAttributeId> appRolePermissionEvaluator =
        new AppRolePermissionEvaluator<ReferenceAttributeId>(
            ImmutableMultimap.<AppRole, Permission>builder()
                .putAll(AppRole.ADMIN, Permission.values())
                .putAll(AppRole.SUPERUSER, Permission.values()).build());

    PermissionEvaluator<ReferenceAttributeId> textAttributeIdPermissionEvaluator =
        new DaoPermissionEvaluator<ReferenceAttributeId>(
            referenceAttributePermissionSystemDao);

    return new DisjunctionPermissionEvaluator<ReferenceAttributeId>(
        appRolePermissionEvaluator, textAttributeIdPermissionEvaluator);
  }

  @Bean
  public PermissionEvaluator<Specification<ResourceId, Resource>> resourceSpecificationPermissionEvaluator() {
    // FIXME: 13/09/16
    return new PermitAllPermissionEvaluator<Specification<ResourceId, Resource>>();
  }

  // System DAOs

  @Bean
  public SystemDao<String, User> userSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcUserDao(dataSource));
  }

  @Bean
  public SystemDao<UserSchemeRoleId, Void> userSchemeRoleSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcUserSchemeRoleDao(dataSource));
  }

  @Bean
  public SystemDao<String, Property> propertySystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcPropertyDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<String>, LangValue> propertyPropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcPropertyPropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<UUID, Scheme> schemeSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemeDao(dataSource));
  }

  @Bean
  public SystemDao<SchemeRole, Void> schemeRoleSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemeRoleDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<UUID>, LangValue> schemePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcSchemePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ClassId, Class> classSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ClassId>, Void> classPermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassPermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ClassId>, LangValue> classPropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcClassPropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcReferenceAttributePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcTextAttributePropertyValueDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceId, Resource> resourceSystemDao(DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceAttributeValueId, ResourceId> resourceReferenceAttributeValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceReferenceAttributeValueDao(dataSource));
  }

  @Bean
  public SystemDao<ResourceAttributeValueId, StrictLangValue> resourceTextAttributeValueSystemDao(
      DataSource dataSource) {
    return CachedSystemDao.create(new JdbcResourceTextAttributeValueDao(dataSource));
  }

}
