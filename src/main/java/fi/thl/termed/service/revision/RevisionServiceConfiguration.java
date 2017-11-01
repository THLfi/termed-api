package fi.thl.termed.service.revision;

import static fi.thl.termed.util.dao.AuthorizedDao.ReportLevel.THROW;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.service.revision.internal.JdbcRevisionDao;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.DaoForwardingRepository;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.TransactionalService;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RevisionServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Bean
  public Service<Long, Revision> revisionService() {
    SystemDao<Long, Revision> dao =
        new CachedSystemDao<>(new JdbcRevisionDao(dataSource));

    PermissionEvaluator<Long> permissionEvaluator = (u, o, p) -> u.getAppRole() == AppRole.USER;

    Service<Long, Revision> service =
        new DaoForwardingRepository<>(
            new AuthorizedDao<>(dao, permissionEvaluator, THROW));

    return new TransactionalService<>(service, transactionManager);
  }

}
