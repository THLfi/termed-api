package fi.thl.termed.service.class_;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.service.class_.internal.ClassRepository;
import fi.thl.termed.service.class_.internal.InitializingClassService;
import fi.thl.termed.service.class_.internal.JdbcClassDao;
import fi.thl.termed.service.class_.internal.JdbcClassPermissionsDao;
import fi.thl.termed.service.class_.internal.JdbcClassPropertyValueDao;
import fi.thl.termed.service.class_.internal.JdbcReferenceAttributeDao;
import fi.thl.termed.service.class_.internal.JdbcReferenceAttributePermissionsDao;
import fi.thl.termed.service.class_.internal.JdbcReferenceAttributePropertyValueDao;
import fi.thl.termed.service.class_.internal.JdbcTextAttributeDao;
import fi.thl.termed.service.class_.internal.JdbcTextAttributePermissionsDao;
import fi.thl.termed.service.class_.internal.JdbcTextAttributePropertyValueDao;
import fi.thl.termed.service.class_.internal.ReferenceAttributeRepository;
import fi.thl.termed.service.class_.internal.TextAttributeRepository;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.DaoPermissionEvaluator;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.LoggingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

import static fi.thl.termed.util.dao.AuthorizedDao.ReportLevel.SILENT;

@Configuration
public class ClassServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  // permission system DAO instances are shared internally
  private SystemDao<ObjectRolePermission<ClassId>, GrantedPermission>
      classPermissionSystemDao;
  private SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission>
      textAttributePermissionSystemDao;
  private SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission>
      referenceAttributePermissionSystemDao;

  @Bean
  public Service<ClassId, Class> classService() {
    Service<ClassId, Class> service = classRepository();

    service = new TransactionalService<>(service, transactionManager);
    service = new LoggingService<>(service, getClass().getPackage().getName() + ".Service");
    service = new InitializingClassService(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<ClassId> classEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(classPermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(textAttributePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(referenceAttributePermissionSystemDao()));
  }

  private AbstractRepository<ClassId, Class> classRepository() {
    return new ClassRepository(
        classDao(),
        classPermissionDao(),
        classPropertyDao(),
        textAttributeRepository(),
        referenceAttributeRepository());
  }

  private Dao<ClassId, Class> classDao() {
    return new AuthorizedDao<>(classSystemDao(), classEvaluator());
  }

  private Dao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionDao() {
    return new AuthorizedDao<>(classPermissionSystemDao(), appAdminEvaluator(), SILENT);
  }

  private Dao<PropertyValueId<ClassId>, LangValue> classPropertyDao() {
    return new AuthorizedDao<>(classPropertySystemDao(), classPropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<ClassId>> classPropertyEvaluator() {
    return (u, o, p) -> classEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<ClassId, Class> classSystemDao() {
    return new CachedSystemDao<>(new JdbcClassDao(dataSource));
  }

  private SystemDao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionSystemDao() {
    if (classPermissionSystemDao == null) {
      classPermissionSystemDao = new CachedSystemDao<>(new JdbcClassPermissionsDao(dataSource));
    }
    return classPermissionSystemDao;
  }

  private SystemDao<PropertyValueId<ClassId>, LangValue> classPropertySystemDao() {
    return new CachedSystemDao<>(new JdbcClassPropertyValueDao(dataSource));
  }

  // text attributes

  private AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository() {
    return new TextAttributeRepository(
        textAttributeDao(),
        textAttributePermissionDao(),
        textAttributePropertyDao());
  }

  private Dao<TextAttributeId, TextAttribute> textAttributeDao() {
    return new AuthorizedDao<>(textAttributeSystemDao(), textAttributeEvaluator(), SILENT);
  }

  private Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionDao() {
    return new AuthorizedDao<>(textAttributePermissionSystemDao(), appAdminEvaluator(), SILENT);
  }

  private Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyDao() {
    return new AuthorizedDao<>(textAttributePropertySystemDao(), textAttributePropertyEvaluator());
  }


  private PermissionEvaluator<PropertyValueId<TextAttributeId>> textAttributePropertyEvaluator() {
    return (u, o, p) -> textAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao() {
    return new CachedSystemDao<>(new JdbcTextAttributeDao(dataSource));
  }

  private SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionSystemDao() {
    if (textAttributePermissionSystemDao == null) {
      textAttributePermissionSystemDao =
          new CachedSystemDao<>(new JdbcTextAttributePermissionsDao(dataSource));
    }
    return textAttributePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertySystemDao() {
    return new CachedSystemDao<>(new JdbcTextAttributePropertyValueDao(dataSource));
  }

  // reference attributes

  private AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository() {
    return new ReferenceAttributeRepository(
        referenceAttributeDao(),
        referenceAttributePermissionDao(),
        referenceAttributePropertyDao());
  }

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao() {
    return new AuthorizedDao<>(referenceAttributeSystemDao(),
                               referenceAttributeEvaluator(), SILENT);
  }

  private Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionDao() {
    return new AuthorizedDao<>(referenceAttributePermissionSystemDao(),
                               appAdminEvaluator(), SILENT);
  }

  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyDao() {
    return new AuthorizedDao<>(referenceAttributePropertySystemDao(),
                               referenceAttributePropertyEvaluator());
  }

  private PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> referenceAttributePropertyEvaluator() {
    return (u, o, p) -> referenceAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  private SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao() {
    return new CachedSystemDao<>(new JdbcReferenceAttributeDao(dataSource));
  }

  private SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionSystemDao() {
    if (referenceAttributePermissionSystemDao == null) {
      referenceAttributePermissionSystemDao =
          new CachedSystemDao<>(new JdbcReferenceAttributePermissionsDao(dataSource));
    }
    return referenceAttributePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertySystemDao() {
    return new CachedSystemDao<>(new JdbcReferenceAttributePropertyValueDao(dataSource));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
                                         user.getAppRole() == AppRole.SUPERUSER;
  }

}
