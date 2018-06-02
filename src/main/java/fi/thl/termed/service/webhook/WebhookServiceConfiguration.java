package fi.thl.termed.service.webhook;

import static fi.thl.termed.util.EventBusUtils.register;
import static fi.thl.termed.util.dao.CachedSystemDao2.cache;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Webhook;
import fi.thl.termed.service.webhook.internal.JdbcWebhookDao;
import fi.thl.termed.service.webhook.internal.NodeEventPostingService;
import fi.thl.termed.util.dao.AuthorizedDao2;
import fi.thl.termed.util.dao.SystemDao2;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.service.DaoForwardingRepository2;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.TransactionalService2;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class WebhookServiceConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private Gson gson;

  @Autowired
  private EventBus eventBus;

  @Bean
  public Service2<UUID, Webhook> webhookService() {
    SystemDao2<UUID, Webhook> dao = register(eventBus, cache(new JdbcWebhookDao(dataSource)));

    PermissionEvaluator<UUID> permissionEvaluator =
        (u, o, p) -> u.getAppRole() == AppRole.ADMIN || u.getAppRole() == AppRole.SUPERUSER;

    Service2<UUID, Webhook> service =
        new DaoForwardingRepository2<>(
            new AuthorizedDao2<>(dao, permissionEvaluator));

    return new TransactionalService2<>(service, transactionManager);
  }

  @Bean
  public NodeEventPostingService eventPostingService(Service2<UUID, Webhook> webhookService) {
    NodeEventPostingService service = new NodeEventPostingService(webhookService(), gson);
    eventBus.register(service);
    return service;
  }

}
