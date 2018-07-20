package fi.thl.termed.service.dump;

import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.DumpId;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.TransactionalService2;
import fi.thl.termed.util.service.WriteErrorHandlingService2;
import fi.thl.termed.util.service.WriteLoggingService2;
import fi.thl.termed.util.service.WritePreAuthorizingService2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DumpServiceConfiguration {

  @Autowired
  private Service2<GraphId, Graph> graphService;

  @Autowired
  private Service2<TypeId, Type> typeService;

  @Autowired
  private Service2<NodeId, Node> nodeService;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private EventBus eventBus;

  @Bean
  public Service2<DumpId, Dump> dumpService() {
    Service2<DumpId, Dump> service =
        new DelegatingDumpService(graphService, typeService, nodeService);

    service = new TransactionalService2<>(service, transactionManager);
    service = new WriteErrorHandlingService2<>(service, (x) -> {
    }, () -> eventBus.post(new InvalidateCachesEvent()));
    service = new WritePreAuthorizingService2<>(service,
        (user) -> user.getAppRole() == SUPERUSER || user.getAppRole() == ADMIN,
        (user) -> false);
    service = new WriteLoggingService2<>(service, getClass().getPackage().getName() + ".Service");

    return service;
  }

}
