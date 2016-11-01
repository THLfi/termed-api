package fi.thl.termed.service.graph.internal;

import java.util.List;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

public class InitializingGraphService extends ForwardingService<GraphId, Graph> {

  public InitializingGraphService(Service<GraphId, Graph> delegate) {
    super(delegate);
  }

  @Override
  public List<GraphId> save(List<Graph> graphs, User currentUser) {
    graphs.forEach(this::initialize);
    return super.save(graphs, currentUser);
  }

  @Override
  public GraphId save(Graph graph, User currentUser) {
    initialize(graph);
    return super.save(graph, currentUser);
  }

  private void initialize(Graph graph) {
    if (graph.getId() == null) {
      graph.setId(coalesce(UUIDs.nameUUIDFromString(graph.getCode()),
                           UUIDs.nameUUIDFromString(graph.getUri()),
                           randomUUID()));
    }
  }

}
