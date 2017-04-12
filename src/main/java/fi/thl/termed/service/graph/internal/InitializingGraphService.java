package fi.thl.termed.service.graph.internal;

import static fi.thl.termed.util.ObjectUtils.coalesce;
import static java.util.UUID.randomUUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.Map;

public class InitializingGraphService extends ForwardingService<GraphId, Graph> {

  public InitializingGraphService(Service<GraphId, Graph> delegate) {
    super(delegate);
  }

  @Override
  public List<GraphId> save(List<Graph> graphs, Map<String, Object> args, User currentUser) {
    graphs.forEach(this::initialize);
    return super.save(graphs, args, currentUser);
  }

  @Override
  public GraphId save(Graph graph, Map<String, Object> args, User currentUser) {
    initialize(graph);
    return super.save(graph, args, currentUser);
  }

  private void initialize(Graph graph) {
    if (graph.getId() == null) {
      graph.setId(coalesce(UUIDs.nameUUIDFromString(graph.getCode()),
          UUIDs.nameUUIDFromString(graph.getUri()),
          randomUUID()));
    }
  }

}
