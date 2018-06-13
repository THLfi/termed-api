package fi.thl.termed.service.graph;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;

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
public class GraphServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  private SystemDao2<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionSystemDao;

  @Bean
  public Service2<GraphId, Graph> graphService() {
    Service2<GraphId, Graph> service = graphRepository();

    service = new TransactionalService2<>(service, transactionManager);
    service = new WriteLoggingService2<>(service, getClass().getPackage().getName() + ".Service");
    service = new InitializingGraphService(service);

    return service;
  }

  @Bean
  public PermissionEvaluator<GraphId> graphEvaluator() {
    return new DisjunctionPermissionEvaluator<>(
        appAdminEvaluator(), new DaoPermissionEvaluator2<>(graphPermissionSystemDao()));
  }

  private Service2<GraphId, Graph> graphRepository() {
    return new GraphRepository(
        graphDao(),
        graphRoleDao(),
        graphPermissionDao(),
        graphPropertyDao());
  }

  private Dao2<GraphId, Graph> graphDao() {
    return new AuthorizedDao2<>(graphSystemDao(), graphEvaluator());
  }

  private Dao2<GraphRole, Empty> graphRoleDao() {
    return new AuthorizedDao2<>(graphRoleSystemDao(), appAdminEvaluator());
  }

  private Dao2<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionDao() {
    return new AuthorizedDao2<>(graphPermissionSystemDao(), appAdminEvaluator());
  }

  private Dao2<PropertyValueId<GraphId>, LangValue> graphPropertyDao() {
    return new AuthorizedDao2<>(graphPropertySystemDao(),
        (u, o, p) -> graphEvaluator().hasPermission(u, o.getSubjectId(), p));
  }

  private SystemDao2<GraphId, Graph> graphSystemDao() {
    return register(eventBus, cache(new JdbcGraphDao(dataSource)));
  }

  private SystemDao2<GraphRole, Empty> graphRoleSystemDao() {
    return register(eventBus, cache(new JdbcGraphRoleDao(dataSource)));
  }

  // this instance is shared internally between other DAOs and evaluators
  private SystemDao2<ObjectRolePermission<GraphId>, GrantedPermission> graphPermissionSystemDao() {
    if (graphPermissionSystemDao == null) {
      graphPermissionSystemDao = register(eventBus, cache(new JdbcGraphPermissionsDao(dataSource)));
    }
    return graphPermissionSystemDao;
  }

  private SystemDao2<PropertyValueId<GraphId>, LangValue> graphPropertySystemDao() {
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
