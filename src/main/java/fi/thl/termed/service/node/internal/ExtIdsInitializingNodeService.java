package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.query.AndSpecification.and;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByCode;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.node.specification.NodesByUri;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.NamedSequenceService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ExtIdsInitializingNodeService extends ForwardingService<NodeId, Node> {

  private NamedSequenceService<TypeId> nodeSequenceService;
  private BiFunction<TypeId, User, Type> typeSource;
  private BiFunction<GraphId, User, Graph> graphSource;

  public ExtIdsInitializingNodeService(Service<NodeId, Node> delegate,
      NamedSequenceService<TypeId> nodeSequenceService,
      BiFunction<TypeId, User, Optional<Type>> typeSource,
      BiFunction<GraphId, User, Optional<Graph>> graphSource) {
    super(delegate);
    this.nodeSequenceService = nodeSequenceService;
    this.typeSource = (typeId, user) -> typeSource.apply(typeId, user)
        .orElseThrow(IllegalStateException::new);
    this.graphSource = (graphId, user) -> graphSource.apply(graphId, user)
        .orElseThrow(IllegalStateException::new);
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    return super.save(nodes.map(n -> addExternalIdentifiers(n, user)), mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(addExternalIdentifiers(node, user), mode, opts, user);
  }

  private Node addExternalIdentifiers(Node node, User user) {
    Optional<Node> cachedNode = findCachedNode(node.identifier(), user);

    if (cachedNode.isPresent()) {
      node.setNumber(cachedNode.get().getNumber());
    } else {
      Long number = nodeSequenceService.getAndAdvance(node.getType(), user);
      node.setNumber(number);

      if (node.getCode() == null) {
        Type type = typeSource.apply(node.getType(), user);
        String code = type.getNodeCodePrefixOrDefault() + number;
        if (existsNodeWithCode(node.getType(), code, user)) {
          node.setCode(code);
        }
      }

      if (node.getUri() == null) {
        Graph graph = graphSource.apply(node.getTypeGraph(), user);
        Optional<String> uri = graph.getUri().map(ns -> ns + node.getCode());
        if (uri.isPresent() && !existsNodeWithUri(node.getType(), uri.get(), user)) {
          node.setUri(uri.get());
        }
      }
    }

    return node;
  }

  private Optional<Node> findCachedNode(NodeId nodeId, User user) {
    try (Stream<Node> nodeStream = values(new Query<>(and(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId()))), user)) {
      return nodeStream.findAny();
    }
  }

  private boolean existsNodeWithCode(TypeId typeId, String code, User user) {
    return count(and(
        new NodesByGraphId(typeId.getGraphId()),
        new NodesByTypeId(typeId.getId()),
        new NodesByCode(code)), user) > 0;
  }

  private boolean existsNodeWithUri(TypeId typeId, String uri, User user) {
    return count(and(
        new NodesByGraphId(typeId.getGraphId()),
        new NodesByTypeId(typeId.getId()),
        new NodesByUri(uri)), user) > 0;
  }

}
