package fi.thl.termed.service.user;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.service.user.internal.JdbcUserDao;
import fi.thl.termed.service.user.internal.JdbcUserGraphRoleDao;
import fi.thl.termed.service.user.internal.UserRepository;
import fi.thl.termed.util.dao.AuthorizedDao2;
import fi.thl.termed.util.dao.SystemDao2;
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
public class UserServiceConfiguration {

  @Autowired
  private EventBus eventBus;

  @Bean
  public Service2<String, User> userService(
      DataSource dataSource, PlatformTransactionManager transactionManager) {

    SystemDao2<String, User> userDao =
        register(eventBus, cache(new JdbcUserDao(dataSource)));
    SystemDao2<UserGraphRole, Empty> userGraphRoleDao =
        register(eventBus, cache(new JdbcUserGraphRoleDao(dataSource)));

    PermissionEvaluator<String> userPermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;
    PermissionEvaluator<UserGraphRole> userGraphRolePermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;

    Service2<String, User> service =
        new UserRepository(
            new AuthorizedDao2<>(userDao, userPermissionEvaluator),
            new AuthorizedDao2<>(userGraphRoleDao, userGraphRolePermissionEvaluator));

    service = new WriteLoggingService2<>(service, getClass().getPackage().getName() + ".Service");

    return new TransactionalService2<>(service, transactionManager);
  }

}
