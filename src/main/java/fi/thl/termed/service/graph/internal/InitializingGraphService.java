package fi.thl.termed.service.graph.internal;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.util.List;

public class InitializingGraphService extends ForwardingService<GraphId, Graph> {

  public InitializingGraphService(Service<GraphId, Graph> delegate) {
    super(delegate);
  }

  @Override
  public List<GraphId> save(List<Graph> graphs, User user, Arg... args) {
    graphs.forEach(this::initialize);
    return super.save(graphs, user, args);
  }

  @Override
  public GraphId save(Graph graph, User user, Arg... args) {
    initialize(graph);
    return super.save(graph, user, args);
  }

  private void initialize(Graph graph) {
    if (graph.getId() == null) {
      graph.setId(coalesce(UUIDs.nameUUIDFromString(graph.getCode()),
          UUIDs.nameUUIDFromString(graph.getUri()),
          randomUUID()));
    }
  }

}
