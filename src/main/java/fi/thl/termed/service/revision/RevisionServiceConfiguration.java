package fi.thl.termed.service.revision;

import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static fi.thl.termed.util.dao.AuthorizedDao.ReportLevel.THROW;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.service.revision.internal.JdbcRevisionDao;
import fi.thl.termed.util.dao.AuthorizedDao;
import fi.thl.termed.util.dao.CachedSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.DaoForwardingRepository;
import fi.thl.termed.util.service.JdbcSequenceService;
import fi.thl.termed.util.service.SequenceService;
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
  public SequenceService revisionSeqService() {
    return new JdbcSequenceService(dataSource, "revision_seq", revisionSeqEvaluator());
  }

  @Bean
  public Service<Long, Revision> revisionService() {
    SystemDao<Long, Revision> dao =
        new CachedSystemDao<>(new JdbcRevisionDao(dataSource));

    Service<Long, Revision> service =
        new DaoForwardingRepository<>(
            new AuthorizedDao<>(dao, revisionEvaluator(), THROW));

    return new TransactionalService<>(service, transactionManager);
  }

  private PermissionEvaluator<String> revisionSeqEvaluator() {
    return new DisjunctionPermissionEvaluator<>(appAdminEvaluator(),
        (u, o, p) -> (p == READ || p == UPDATE));
  }

  private PermissionEvaluator<Long> revisionEvaluator() {
    return new DisjunctionPermissionEvaluator<>(appAdminEvaluator(),
        (u, o, p) -> p == INSERT);
  }

  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) ->
        user.getAppRole() == AppRole.ADMIN || user.getAppRole() == AppRole.SUPERUSER;
  }

}
