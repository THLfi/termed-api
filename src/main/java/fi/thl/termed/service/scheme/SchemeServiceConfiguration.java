package fi.thl.termed.service.scheme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.service.scheme.internal.InitializingSchemeService;
import fi.thl.termed.service.scheme.internal.JdbcSchemeDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemePermissionsDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemePropertyValueDao;
import fi.thl.termed.service.scheme.internal.JdbcSchemeRoleDao;
import fi.thl.termed.service.scheme.internal.SchemeRepository;
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
public class SchemeServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  private SystemDao<ObjectRolePermission<SchemeId>, GrantedPermission> schemePermissionSystemDao;

  @Bean
  public Service<SchemeId, Scheme> schemeService() {
    Service<SchemeId, Scheme> service = schemeRepository();

    service = new TransactionalService<>(service, transactionManager);
    service = new LoggingService<>(service, getClass().getPackage().getName() + ".Service");
    service = new InitializingSchemeService(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<SchemeId> schemeEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(schemePermissionSystemDao()));
  }

  private AbstractRepository<SchemeId, Scheme> schemeRepository() {
    return new SchemeRepository(
        schemeDao(),
        schemeRoleDao(),
        schemePermissionDao(),
        schemePropertyDao());
  }

  private Dao<SchemeId, Scheme> schemeDao() {
    return new AuthorizedDao<>(schemeSystemDao(), schemeEvaluator());
  }

  private Dao<SchemeRole, Empty> schemeRoleDao() {
    return new AuthorizedDao<>(schemeRoleSystemDao(), appAdminEvaluator(), SILENT);
  }

  private Dao<ObjectRolePermission<SchemeId>, GrantedPermission> schemePermissionDao() {
    return new AuthorizedDao<>(schemePermissionSystemDao(), appAdminEvaluator(), SILENT);
  }

  private Dao<PropertyValueId<SchemeId>, LangValue> schemePropertyDao() {
    return new AuthorizedDao<>(
        schemePropertySystemDao(),
        (u, o, p) -> schemeEvaluator().hasPermission(u, o.getSubjectId(), p));
  }

  private SystemDao<SchemeId, Scheme> schemeSystemDao() {
    return new CachedSystemDao<>(new JdbcSchemeDao(dataSource));
  }

  private SystemDao<SchemeRole, Empty> schemeRoleSystemDao() {
    return new CachedSystemDao<>(new JdbcSchemeRoleDao(dataSource));
  }

  // this instance is shared internally between other DAOs and evaluators
  private SystemDao<ObjectRolePermission<SchemeId>, GrantedPermission> schemePermissionSystemDao() {
    if (schemePermissionSystemDao == null) {
      schemePermissionSystemDao = new CachedSystemDao<>(new JdbcSchemePermissionsDao(dataSource));
    }
    return schemePermissionSystemDao;
  }

  private SystemDao<PropertyValueId<SchemeId>, LangValue> schemePropertySystemDao() {
    return new CachedSystemDao<>(new JdbcSchemePropertyValueDao(dataSource));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
                                         user.getAppRole() == AppRole.SUPERUSER;
  }

}
