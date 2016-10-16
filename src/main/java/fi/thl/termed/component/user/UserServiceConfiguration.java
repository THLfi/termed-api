package fi.thl.termed.component.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

@Configuration
public class UserServiceConfiguration {

  @Bean
  public Service<String, User> userService(
      DataSource dataSource, PlatformTransactionManager transactionManager) {

    SystemDao<String, User> userSystemDao =
        new CachedSystemDao<>(new JdbcUserDao(dataSource));
    SystemDao<UserSchemeRoleId, Empty> userSchemeRoleSystemDao =
        new CachedSystemDao<>(new JdbcUserSchemeRoleDao(dataSource));

    PermissionEvaluator<String> userPermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;
    PermissionEvaluator<UserSchemeRoleId> userSchemeRolePermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;

    Service<String, User> service =
        new UserRepositoryService(
            new AuthorizedDao<>(userSystemDao, userPermissionEvaluator),
            new AuthorizedDao<>(userSchemeRoleSystemDao, userSchemeRolePermissionEvaluator));

    return new TransactionalService<>(service, transactionManager);
  }

}
