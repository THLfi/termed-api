package fi.thl.termed.service.type;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;
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
import fi.thl.termed.util.dao.AuthorizedDao2;
import fi.thl.termed.util.dao.Dao2;
import fi.thl.termed.util.dao.SystemDao2;
import fi.thl.termed.util.permission.DaoPermissionEvaluator2;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.TransactionalService2;
import fi.thl.termed.util.service.WriteLoggingService2;
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
  private SystemDao2<ObjectRolePermission<TypeId>, GrantedPermission>
      typePermissionSystemDao;
  private SystemDao2<ObjectRolePermission<TextAttributeId>, GrantedPermission>
      textAttributePermissionSystemDao;
  private SystemDao2<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission>
      referenceAttributePermissionSystemDao;

  @Bean
  public Service2<TypeId, Type> typeService() {
    Service2<TypeId, Type> service = typeRepository();

    service = new TransactionalService2<>(service, transactionManager);
    service = new WriteLoggingService2<>(service, getClass().getPackage().getName() + ".Service");
    service = new InitializingTypeService(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<TypeId> typeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator2<>(typePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator2<>(textAttributePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(),
        new DaoPermissionEvaluator2<>(referenceAttributePermissionSystemDao()));
  }

  private Service2<TypeId, Type> typeRepository() {
    return new TypeRepository(
        typeDao(),
        typePermissionDao(),
        typePropertyDao(),
        textAttributeRepository(),
        referenceAttributeRepository(),
        getDatabaseProductName(dataSource).equals("db/migration/postgresql"));
  }

  private Dao2<TypeId, Type> typeDao() {
    return new AuthorizedDao2<>(typeSystemDao(), typeEvaluator());
  }

  private Dao2<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao() {
    return new AuthorizedDao2<>(typePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao2<PropertyValueId<TypeId>, LangValue> typePropertyDao() {
    return new AuthorizedDao2<>(typePropertySystemDao(), typePropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<TypeId>> typePropertyEvaluator() {
    return (u, o, p) -> typeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao2<TypeId, Type> typeSystemDao() {
    return register(eventBus, cache(new JdbcTypeDao(dataSource)));
  }

  private SystemDao2<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionSystemDao() {
    if (typePermissionSystemDao == null) {
      typePermissionSystemDao = register(eventBus, cache(new JdbcTypePermissionsDao(dataSource)));
    }
    return typePermissionSystemDao;
  }

  private SystemDao2<PropertyValueId<TypeId>, LangValue> typePropertySystemDao() {
    return register(eventBus, cache(new JdbcTypePropertyDao(dataSource)));
  }

  // text attributes

  private Service2<TextAttributeId, TextAttribute> textAttributeRepository() {
    return new TextAttributeRepository(
        textAttributeDao(),
        textAttributePermissionDao(),
        textAttributePropertyDao());
  }

  private Dao2<TextAttributeId, TextAttribute> textAttributeDao() {
    return new AuthorizedDao2<>(textAttributeSystemDao(), textAttributeEvaluator());
  }

  private Dao2<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionDao() {
    return new AuthorizedDao2<>(textAttributePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao2<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyDao() {
    return new AuthorizedDao2<>(textAttributePropertySystemDao(), textAttributePropertyEvaluator());
  }


  private PermissionEvaluator<PropertyValueId<TextAttributeId>> textAttributePropertyEvaluator() {
    return (u, o, p) -> textAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao2<TextAttributeId, TextAttribute> textAttributeSystemDao() {
    return register(eventBus, register(eventBus, cache(new JdbcTextAttributeDao(dataSource))));
  }

  private SystemDao2<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionSystemDao() {
    if (textAttributePermissionSystemDao == null) {
      textAttributePermissionSystemDao = register(eventBus, cache(
          new JdbcTextAttributePermissionsDao(dataSource)));
    }
    return textAttributePermissionSystemDao;
  }

  private SystemDao2<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertySystemDao() {
    return register(eventBus, cache(new JdbcTextAttributePropertyDao(dataSource)));
  }

  // reference attributes

  private Service2<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository() {
    return new ReferenceAttributeRepository(
        referenceAttributeDao(),
        referenceAttributePermissionDao(),
        referenceAttributePropertyDao());
  }

  private Dao2<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao() {
    return new AuthorizedDao2<>(referenceAttributeSystemDao(), referenceAttributeEvaluator());
  }

  private Dao2<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionDao() {
    return new AuthorizedDao2<>(referenceAttributePermissionSystemDao(), appAdminEvaluator());
  }

  private Dao2<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyDao() {
    return new AuthorizedDao2<>(referenceAttributePropertySystemDao(),
        referenceAttributePropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> referenceAttributePropertyEvaluator() {
    return (u, o, p) -> referenceAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao2<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao() {
    return register(eventBus, cache(new JdbcReferenceAttributeDao(dataSource)));
  }

  private SystemDao2<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionSystemDao() {
    if (referenceAttributePermissionSystemDao == null) {
      referenceAttributePermissionSystemDao = register(eventBus, cache(
          new JdbcReferenceAttributePermissionsDao(dataSource)));
    }
    return referenceAttributePermissionSystemDao;
  }

  private SystemDao2<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertySystemDao() {
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
