package fi.thl.termed.service.graph;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao.cache;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.service.graph.internal.GraphRepository;
import fi.thl.termed.service.graph.internal.InitializingGraphService;
import fi.thl.termed.service.graph.internal.JdbcGraphDao;
import fi.thl.termed.service.graph.internal.JdbcGraphPermissionsDao;
import fi.thl.termed.service.graph.internal.JdbcGraphPropertyDao;
import fi.thl.termed.service.graph.internal.JdbcGraphRoleDao;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.DaoPermissionEvaluator;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.ProfilingService;
import fi.thl.termed.util.service.ReadWriteSynchronizedService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;
import fi.thl.termed.util.service.WriteLoggingService;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class GraphServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  private SystemDao<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionSystemDao;

  @Bean
  public Service<GraphId, Graph> graphService() {
    Service<GraphId, Graph> service = graphRepository();

    service = new TransactionalService<>(service, transactionManager);
    service = new WriteLoggingService<>(service,
        getClass().getPackage().getName() + ".WriteLoggingService");
    service = new ProfilingService<>(service,
        getClass().getPackage().getName() + ".ProfilingService", 500);
    service = new InitializingGraphService(service);
    service = new ReadWriteSynchronizedService<>(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<GraphId> graphEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator<>(graphPermissionSystemDao()));
  }

  private Service<GraphId, Graph> graphRepository() {
    return new GraphRepository(
        graphDao(),
        graphRoleDao(),
        graphPermissionDao(),
        graphPropertyDao());
  }

  private Dao<GraphId, Graph> graphDao() {
    return new AuthorizedDao<>(graphSystemDao(), graphEvaluator());
  }

  private Dao<GraphRole, Empty> graphRoleDao() {
    return new AuthorizedDao<>(graphRoleSystemDao(), appAdminEvaluator());
  }

  private Dao<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionDao() {
    return new AuthorizedDao<>(graphPermissionSystemDao(), appAdminEvaluator());
  }

  private Dao<PropertyValueId<GraphId>, LangValue> graphPropertyDao() {
    return new AuthorizedDao<>(graphPropertySystemDao(),
        (u, o, p) -> graphEvaluator().hasPermission(u, o.getSubjectId(), p));
  }

  private SystemDao<GraphId, Graph> graphSystemDao() {
    return register(eventBus, cache(new JdbcGraphDao(dataSource)));
  }

  private SystemDao<GraphRole, Empty> graphRoleSystemDao() {
    return register(eventBus, cache(new JdbcGraphRoleDao(dataSource)));
  }

  // this instance is shared internally between other DAOs and evaluators
  private SystemDao<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionSystemDao() {
    if (graphPermissionSystemDao == null) {
      graphPermissionSystemDao = register(eventBus, cache(new JdbcGraphPermissionsDao(dataSource)));
    }
    return graphPermissionSystemDao;
  }

  private SystemDao<PropertyValueId<GraphId>, LangValue> graphPropertySystemDao() {
    return register(eventBus, cache(new JdbcGraphPropertyDao(dataSource)));
  }

  /**
   * Creates type specific permission evaluator that accepts users that are admins or superusers
   */
  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) -> user.getAppRole() == AppRole.ADMIN ||
        user.getAppRole() == AppRole.SUPERUSER;
  }

}
