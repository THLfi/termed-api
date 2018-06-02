package fi.thl.termed.service.revision;

import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.service.revision.internal.JdbcRevisionDao;
import fi.thl.termed.util.dao.AuthorizedDao2;
import fi.thl.termed.util.dao.SystemDao2;
import fi.thl.termed.util.permission.DisjunctionPermissionEvaluator;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.DaoForwardingRepository2;
import fi.thl.termed.util.service.JdbcSequenceService;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.TransactionalService2;
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

  @Autowired
  private EventBus eventBus;

  @Bean
  public SequenceService revisionSeqService() {
    return new JdbcSequenceService(dataSource, "revision_seq", revisionSeqEvaluator());
  }

  @Bean
  public Service2<Long, Revision> revisionService() {
    SystemDao2<Long, Revision> dao = register(eventBus, cache(new JdbcRevisionDao(dataSource)));

    Service2<Long, Revision> service =
        new DaoForwardingRepository2<>(
            new AuthorizedDao2<>(dao, revisionEvaluator()));

    return new TransactionalService2<>(service, transactionManager);
  }

  private PermissionEvaluator<String> revisionSeqEvaluator() {
    return new DisjunctionPermissionEvaluator<>(appAdminEvaluator(),
        (u, o, p) -> (p == READ || p == UPDATE));
  }

  private PermissionEvaluator<Long> revisionEvaluator() {
    return new DisjunctionPermissionEvaluator<>(appAdminEvaluator(),
        (u, o, p) -> (p == READ || p == INSERT));
  }

  private <T> PermissionEvaluator<T> appAdminEvaluator() {
    return (user, object, permission) ->
        user.getAppRole() == AppRole.ADMIN || user.getAppRole() == AppRole.SUPERUSER;
  }

}
