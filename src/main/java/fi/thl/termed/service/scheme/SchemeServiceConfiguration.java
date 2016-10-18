package fi.thl.termed.service.scheme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.service.scheme.internal.ClassRepository;
import fi.thl.termed.service.scheme.internal.JdbcClassDao;
import fi.thl.termed.service.scheme.internal.JdbcClassPermissionsDao;
import fi.thl.termed.service.scheme.internal.JdbcClassPropertyValueDao;
import fi.thl.termed.service.scheme.internal.JdbcReferenceAttributeDao;
import fi.thl.termed.service.scheme.internal.JdbcReferenceAttributePermissionsDao;
import fi.thl.termed.service.scheme.internal.JdbcReferenceAttributePropertyValueDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemeDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemePermissionsDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemePropertyValueDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemeRoleDao;
import fi.thl.termed.service.scheme.internal.JdbcTextAttributeDao;
import fi.thl.termed.service.scheme.internal.JdbcTextAttributePermissionsDao;
import fi.thl.termed.service.scheme.internal.JdbcTextAttributePropertyValueDao;
import fi.thl.termed.service.scheme.internal.ReferenceAttributeRepository;
import fi.thl.termed.service.scheme.internal.ResolvingSchemeService;
import fi.thl.termed.service.scheme.internal.SchemeRepository;
import fi.thl.termed.service.scheme.internal.TextAttributeRepository;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
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

@Configuration
public class SchemeServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Bean
  public Service<UUID, Scheme> schemeService() {
    Service<UUID, Scheme> service = schemeRepository();

    service = new TransactionalService<>(service, transactionManager);
    service = new LoggingService<>(service, getClass().getPackage().getName() + ".Service");
    service = new ResolvingSchemeService(service);

    return service;
  }

  private AbstractRepository<UUID, Scheme> schemeRepository() {
    return new SchemeRepository(
        schemeDao(),
        schemeRoleDao(),
        schemePermissionDao(),
        schemePropertyDao(),
        classRepository());
  }

  @Bean
  public Dao<UUID, Scheme> schemeDao() {
    return new AuthorizedDao<>(schemeSystemDao(), schemeEvaluator());
  }

  @Bean
  public Dao<SchemeRole, Empty> schemeRoleDao() {
    return new AuthorizedDao<>(schemeRoleSystemDao(), appAdminEvaluator());
  }

  @Bean
  public Dao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionDao() {
    return new AuthorizedDao<>(schemePermissionSystemDao(), appAdminEvaluator());
  }

  @Bean
  public Dao<PropertyValueId<UUID>, LangValue> schemePropertyDao() {
    return new AuthorizedDao<>(schemePropertySystemDao(), schemePropertyEvaluator());
  }

  @Bean
  public PermissionEvaluator<UUID> schemeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(schemePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<PropertyValueId<UUID>> schemePropertyEvaluator() {
    return (u, o, p) -> schemeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  @Bean
  public SystemDao<UUID, Scheme> schemeSystemDao() {
    return new CachedSystemDao<>(new JdbcSchemeDao(dataSource));
  }

  @Bean
  public SystemDao<SchemeRole, Empty> schemeRoleSystemDao() {
    return new CachedSystemDao<>(new JdbcSchemeRoleDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionSystemDao() {
    return new CachedSystemDao<>(new JdbcSchemePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<UUID>, LangValue> schemePropertySystemDao() {
    return new CachedSystemDao<>(new JdbcSchemePropertyValueDao(dataSource));
  }

  // classes

  @Bean
  public AbstractRepository<ClassId, Class> classRepository() {
    return new ClassRepository(
        classDao(),
        classPermissionDao(),
        classPropertyDao(),
        textAttributeRepository(),
        referenceAttributeRepository());
  }

  @Bean
  public Dao<ClassId, Class> classDao() {
    return new AuthorizedDao<>(classSystemDao(), classEvaluator());
  }

  @Bean
  public Dao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionDao() {
    return new AuthorizedDao<>(classPermissionSystemDao(), appAdminEvaluator());
  }

  @Bean
  public Dao<PropertyValueId<ClassId>, LangValue> classPropertyDao() {
    return new AuthorizedDao<>(classPropertySystemDao(), classPropertyEvaluator());
  }

  @Bean
  public PermissionEvaluator<ClassId> classEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(classPermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<PropertyValueId<ClassId>> classPropertyEvaluator() {
    return (u, o, p) -> classEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  @Bean
  public SystemDao<ClassId, Class> classSystemDao() {
    return new CachedSystemDao<>(new JdbcClassDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionSystemDao() {
    return new CachedSystemDao<>(new JdbcClassPermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ClassId>, LangValue> classPropertySystemDao() {
    return new CachedSystemDao<>(new JdbcClassPropertyValueDao(dataSource));
  }

  // text attributes

  @Bean
  public AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository() {
    return new TextAttributeRepository(
        textAttributeDao(),
        textAttributePermissionDao(),
        textAttributePropertyDao());
  }

  @Bean
  public Dao<TextAttributeId, TextAttribute> textAttributeDao() {
    return new AuthorizedDao<>(textAttributeSystemDao(), textAttributeEvaluator());
  }

  @Bean
  public Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionDao() {
    return new AuthorizedDao<>(textAttributePermissionSystemDao(), appAdminEvaluator());
  }

  @Bean
  public Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyDao() {
    return new AuthorizedDao<>(textAttributePropertySystemDao(), textAttributePropertyEvaluator());
  }

  @Bean
  public PermissionEvaluator<TextAttributeId> textAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(textAttributePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<PropertyValueId<TextAttributeId>> textAttributePropertyEvaluator() {
    return (u, o, p) -> textAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  @Bean
  public SystemDao<TextAttributeId, TextAttribute> textAttributeSystemDao() {
    return new CachedSystemDao<>(new JdbcTextAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> textAttributePermissionSystemDao() {
    return new CachedSystemDao<>(new JdbcTextAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertySystemDao() {
    return new CachedSystemDao<>(new JdbcTextAttributePropertyValueDao(dataSource));
  }

  // reference attributes

  @Bean
  public AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository() {
    return new ReferenceAttributeRepository(
        referenceAttributeDao(),
        referenceAttributePermissionDao(),
        referenceAttributePropertyDao());
  }

  @Bean
  public Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao() {
    return new AuthorizedDao<>(referenceAttributeSystemDao(), referenceAttributeEvaluator());
  }

  @Bean
  public Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionDao() {
    return new AuthorizedDao<>(referenceAttributePermissionSystemDao(), appAdminEvaluator());
  }

  @Bean
  public Dao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertyDao() {
    return new AuthorizedDao<>(referenceAttributePropertySystemDao(),
                               referenceAttributePropertyEvaluator());
  }

  @Bean
  public PermissionEvaluator<ReferenceAttributeId> referenceAttributeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(referenceAttributePermissionSystemDao()));
  }

  @Bean
  public PermissionEvaluator<PropertyValueId<ReferenceAttributeId>> referenceAttributePropertyEvaluator() {
    return (u, o, p) -> referenceAttributeEvaluator().hasPermission(u, o.getSubjectId(), p);
  }

  @Bean
  public SystemDao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeSystemDao() {
    return new CachedSystemDao<>(new JdbcReferenceAttributeDao(dataSource));
  }

  @Bean
  public SystemDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> referenceAttributePermissionSystemDao() {
    return new CachedSystemDao<>(new JdbcReferenceAttributePermissionsDao(dataSource));
  }

  @Bean
  public SystemDao<PropertyValueId<ReferenceAttributeId>, LangValue> referenceAttributePropertySystemDao() {
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
