package fi.thl.termed.service.dump;

import static fi.thl.termed.util.query.OrSpecification.or;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.DumpId;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.graph.specification.GraphById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Optional;
import java.util.stream.Stream;

class DelegatingDumpService implements Service<DumpId, Dump> {

  private Service<GraphId, Graph> graphService;
  private Service<TypeId, Type> typeService;
  private Service<NodeId, Node> nodeService;

  DelegatingDumpService(
      Service<GraphId, Graph> graphService,
      Service<TypeId, Type> typeService,
      Service<NodeId, Node> nodeService) {
    this.graphService = graphService;
    this.typeService = typeService;
    this.nodeService = nodeService;
  }

  @Override
  public void save(Stream<Dump> dumps, SaveMode mode, WriteOptions opts, User user) {
    try (Stream<Dump> closeable = dumps) {
      closeable.forEach(dump -> save(dump, mode, opts, user));
    }
  }

  @Override
  public DumpId save(Dump dump, SaveMode mode, WriteOptions opts, User user) {
    graphService.save(dump.getGraphs(), mode, opts, user);
    typeService.save(dump.getTypes(), mode, opts, user);
    nodeService.save(dump.getNodes(), mode, opts, user);
    return dump.identifier();
  }

  @Override
  public Optional<Dump> get(DumpId dumpId, User u, Select... selects) {
    Query<GraphId, Graph> graphSpecification =
        new Query<>(or(dumpId.getGraphIds().stream()
            .map(id -> new GraphById(id.getId()))
            .collect(toList())));
    Query<TypeId, Type> typeSpecification =
        new Query<>(or(dumpId.getGraphIds().stream()
            .map(id -> new TypesByGraphId(id.getId()))
            .collect(toList())));
    Query<NodeId, Node> nodeSpecification =
        new Query<>(or(dumpId.getGraphIds().stream()
            .map(id -> new NodesByGraphId(id.getId()))
            .collect(toList())));

    return Optional.of(new Dump(
        graphService.values(graphSpecification, u),
        typeService.values(typeSpecification, u),
        nodeService.values(nodeSpecification, u)));
  }

  @Override
  public void delete(Stream<DumpId> keys, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(DumpId id, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveAndDelete(Stream<Dump> saves, Stream<DumpId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<Dump> values(Query<DumpId, Dump> query, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long count(Specification<DumpId, Dump> spec, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(DumpId key, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<DumpId> keys(Query<DumpId, Dump> query, User user) {
    throw new UnsupportedOperationException();
  }

}
