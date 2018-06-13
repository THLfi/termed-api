package fi.thl.termed.service.graph.internal;

import static java.util.UUID.randomUUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService2;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.UUID;
import java.util.stream.Stream;

public class InitializingGraphService extends ForwardingService2<GraphId, Graph> {

  public InitializingGraphService(Service2<GraphId, Graph> delegate) {
    super(delegate);
  }

  @Override
  public Stream<GraphId> save(Stream<Graph> graphs, SaveMode mode, WriteOptions opts, User user) {
    return super.save(graphs.map(this::initialize), mode, opts, user);
  }

  @Override
  public GraphId save(Graph graph, SaveMode mode, WriteOptions opts, User user) {
    return super.save(initialize(graph), mode, opts, user);
  }

  private Graph initialize(Graph graph) {
    if (graph.getId() == null) {
      UUID id = graph.getCode()
          .map(UUIDs::nameUUIDFromString)
          .orElse(graph.getUri()
              .map(UUIDs::nameUUIDFromString)
              .orElse(randomUUID()));

      return Graph.builder().id(id)
          .copyOptionalsFrom(graph)
          .build();
    }
    return graph;
  }

}
