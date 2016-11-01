package fi.thl.termed.service.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import fi.thl.termed.service.user.internal.JdbcUserDao;
import fi.thl.termed.service.user.internal.JdbcUserGraphRoleDao;
import fi.thl.termed.service.user.internal.UserRepository;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;

import static fi.thl.termed.util.dao.AuthorizedDao.ReportLevel.THROW;

@Configuration
public class UserServiceConfiguration {

  @Bean
  public Service<String, User> userService(
      DataSource dataSource, PlatformTransactionManager transactionManager) {

    SystemDao<String, User> userDao =
        new CachedSystemDao<>(new JdbcUserDao(dataSource));
    SystemDao<UserGraphRole, Empty> userGraphRoleDao =
        new CachedSystemDao<>(new JdbcUserGraphRoleDao(dataSource));

    PermissionEvaluator<String> userPermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;
    PermissionEvaluator<UserGraphRole> userGraphRolePermissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.SUPERUSER;

    Service<String, User> service =
        new UserRepository(
            new AuthorizedDao<>(userDao, userPermissionEvaluator, THROW),
            new AuthorizedDao<>(userGraphRoleDao, userGraphRolePermissionEvaluator, THROW));

    return new TransactionalService<>(service, transactionManager);
  }

}
