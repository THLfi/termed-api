package fi.thl.termed.service.dump;

import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;

import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.DumpId;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.service.ProfilingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteLoggingService;
import fi.thl.termed.util.service.WritePreAuthorizingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DumpServiceConfiguration {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Bean
  public Service<DumpId, Dump> dumpService() {
    Service<DumpId, Dump> service =
        new DelegatingDumpService(graphService, typeService, nodeService);

    service = new WritePreAuthorizingService<>(service,
        (user) -> user.getAppRole() == SUPERUSER || user.getAppRole() == ADMIN,
        (user) -> false);
    service = new WriteLoggingService<>(service,
        getClass().getPackage().getName() + ".WriteLoggingService");
    service = new ProfilingService<>(service,
        getClass().getPackage().getName() + ".ProfilingService", 0);

    return service;
  }

}
