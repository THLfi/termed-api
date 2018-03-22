package fi.thl.termed.service.dump;

import static fi.thl.termed.util.collect.SetUtils.toImmutableSet;
import static fi.thl.termed.util.query.OrSpecification.or;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Dump;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class DelegatingDumpService implements Service<ImmutableSet<GraphId>, Dump> {

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
  public ImmutableSet<GraphId> save(Dump dump, SaveMode mode, WriteOptions opts, User user) {
    List<Graph> graphs = dump.getGraphs().collect(toList());
    List<Type> types = dump.getTypes().collect(toList());
    List<Node> nodes = dump.getNodes().collect(toList());

    graphService.save(graphs, mode, opts, user);
    typeService.save(types, mode, opts, user);
    nodeService.save(nodes, mode, opts, user);

    return graphs.stream().map(Graph::identifier).collect(toImmutableSet());
  }

  @Override
  public Optional<Dump> get(ImmutableSet<GraphId> ids, User u, Select... selects) {
    Specification<GraphId, Graph> graphSpecification =
        or(ids.stream().map(id -> new GraphById(id.getId())).collect(toList()));
    Specification<TypeId, Type> typeSpecification =
        or(ids.stream().map(id -> new TypesByGraphId(id.getId())).collect(toList()));
    Specification<NodeId, Node> nodeSpecification =
        or(ids.stream().map(id -> new NodesByGraphId(id.getId())).collect(toList()));

    return Optional.of(new Dump(
        graphService.getValueStream(graphSpecification, u),
        typeService.getValueStream(typeSpecification, u),
        nodeService.getValueStream(nodeSpecification, u)));
  }

  @Override
  public void delete(ImmutableSet<GraphId> id, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<Dump> getValueStream(Query<ImmutableSet<GraphId>, Dump> query, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<ImmutableSet<GraphId>> getKeyStream(
      Query<ImmutableSet<GraphId>, Dump> query, User user) {
    throw new UnsupportedOperationException();
  }

}
