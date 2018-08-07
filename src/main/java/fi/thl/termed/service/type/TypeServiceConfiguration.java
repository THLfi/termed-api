package fi.thl.termed.service.type;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao.cache;
import static fi.thl.termed.util.spring.jdbc.SpringJdbcUtils.getDatabaseProductName;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.type.internal.InitializingTypeService;
import fi.thl.termed.service.type.internal.JdbcReferenceAttributeDao;
import fi.thl.termed.service.type.internal.JdbcReferenceAttributePermissionsDao;
import fi.thl.termed.service.type.internal.JdbcReferenceAttributePropertyDao;
import fi.thl.termed.service.type.internal.JdbcTextAttributeDao;
import fi.thl.termed.service.type.internal.JdbcTextAttributePermissionsDao;
import fi.thl.termed.service.type.internal.JdbcTextAttributePropertyDao;
import fi.thl.termed.service.type.internal.JdbcTypeDao;
import fi.thl.termed.service.type.internal.JdbcTypePermissionsDao;
import fi.thl.termed.service.type.internal.JdbcTypePropertyDao;
import fi.thl.termed.service.type.internal.ReferenceAttributeRepository;
import fi.thl.termed.service.type.internal.TextAttributeRepository;
import fi.thl.termed.service.type.internal.TypeRepository;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.DaoPermissionEvaluator;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.ProfilingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;
import fi.thl.termed.util.service.WriteLoggingService;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TypeServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  // permission system DAO instances are shared internally
  private SystemDao<ObjectRolePermission<TypeId>, GrantedPermission>
      typePermissionSystemDao;
  private SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission>
      textAttributePermissionSystemDao;
  private SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission>
      referenceAttributePermissionSystemDao;

  @Bean
  public Service<TypeId, Type> typeService() {
    Service<TypeId, Type> service = typeRepository();

    service = new TransactionalService<>(service, transactionManager);
    service = new WriteLoggingService<>(service,
        getClass().getPackage().getName() + ".WriteLoggingService");
    service = new ProfilingService<>(service,
        getClass().getPackage().getName() + ".ProfilingService", 500);
    service = new InitializingTypeService(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<TypeId> typeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(typePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(textAttributePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        new DaoPermissionEvaluator<>(referenceAttributePermissionSystemDao()));
  }

  private Service<TypeId, Type> typeRepository() {
    return new TypeRepository(
        typeDao(),
        typePermissionDao(),
        typePropertyDao(),
        textAttributeRepository(),
        referenceAttributeRepository(),
        getDatabaseProductName(dataSource).equalsIgnoreCase("postgresql") ? 1 : -1);
  }

  private Dao<TypeId, Type> typeDao() {
    return new AuthorizedDao<>(typeSystemDao(), typeEvaluator());
  }

  private Dao<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao() {
    return new AuthorizedDao<>(typePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao<PropertyValueId<TypeId>, LangValue> typePropertyDao() {
    return new AuthorizedDao<>(typePropertySystemDao(), typePropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<TypeId>> typePropertyEvaluator() {
    return (u, o, p) -> typeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<TypeId, Type> typeSystemDao() {
    return register(eventBus, cache(new JdbcTypeDao(dataSource)));
  }

  private SystemDao<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionSystemDao() {
    if (typePermissionSystemDao == null) {
      typePermissionSystemDao = register(eventBus, cache(new JdbcTypePermissionsDao(dataSource)));
    }
    return typePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<TypeId>, LangValue> typePropertySystemDao() {
    return register(eventBus, cache(new JdbcTypePropertyDao(dataSource)));
  }

  // text attributes

  private Service<TextAttributeId, TextAttribute> textAttributeRepository() {
    return new TextAttributeRepository(
        textAttributeDao(),
        textAttributePermissionDao(),
        textAttributePropertyDao());
  }

  private Dao<TextAttributeId, TextAttribute> textAttributeDao() {
    return new AuthorizedDao<>(textAttributeSystemDao(), textAttributeEvaluator());
  }

  private Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionDao() {
    return new AuthorizedDao<>(textAttributePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyDao() {
    return new AuthorizedDao<>(textAttributePropertySystemDao(), textAttributePropertyEvaluator());
  }


  private PermissionEvaluator<PropertyValueId<TextAttributeId>> textAttributePropertyEvaluator() {
    return (u, o, p) -> textAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao() {
    return register(eventBus, register(eventBus, cache(new JdbcTextAttributeDao(dataSource))));
  }

  private SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionSystemDao() {
    if (textAttributePermissionSystemDao == null) {
      textAttributePermissionSystemDao = register(eventBus, cache(
          new JdbcTextAttributePermissionsDao(dataSource)));
    }
    return textAttributePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertySystemDao() {
    return register(eventBus, cache(new JdbcTextAttributePropertyDao(dataSource)));
  }

  // reference attributes

  private Service<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository() {
    return new ReferenceAttributeRepository(
        referenceAttributeDao(),
        referenceAttributePermissionDao(),
        referenceAttributePropertyDao());
  }

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao() {
    return new AuthorizedDao<>(referenceAttributeSystemDao(), referenceAttributeEvaluator());
  }

  private Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionDao() {
    return new AuthorizedDao<>(referenceAttributePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyDao() {
    return new AuthorizedDao<>(referenceAttributePropertySystemDao(),
        referenceAttributePropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> referenceAttributePropertyEvaluator() {
    return (u, o, p) -> referenceAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao() {
    return register(eventBus, cache(new JdbcReferenceAttributeDao(dataSource)));
  }

  private SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionSystemDao() {
    if (referenceAttributePermissionSystemDao == null) {
      referenceAttributePermissionSystemDao = register(eventBus, cache(
          new JdbcReferenceAttributePermissionsDao(dataSource)));
    }
    return referenceAttributePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertySystemDao() {
    return register(eventBus, cache(new JdbcReferenceAttributePropertyDao(dataSource)));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
        user.getAppRole() == AppRole.SUPERUSER;
  }

}
