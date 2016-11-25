package fi.thl.termed.service.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.dump.internal.DumpService;
import fi.thl.termed.util.service.Service;

@Configuration
public class DumpServiceConfiguration {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Bean
  public DumpService dumpService() {
    return new DumpService(graphService, typeService, nodeService, transactionManager);
  }

}
