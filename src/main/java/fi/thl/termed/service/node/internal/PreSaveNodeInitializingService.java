package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.OrSpecification.or;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Selects;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class PreSaveNodeInitializingService extends ForwardingService<NodeId, Node> {

  PreSaveNodeInitializingService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    switch (mode) {
      case INSERT:
        return super.save(insert(Stream.of(node), opts, user)
                .findFirst()
                .orElseThrow(NotFoundException::new),
            mode, opts, user);
      case UPDATE:
        return super.save(pairAndUpdate(Stream.of(node), opts, user)
                .findFirst()
                .orElseThrow(NotFoundException::new),
            mode, opts, user);
      case UPSERT:
        return super.save(pairAndUpsert(Stream.of(node), opts, user)
                .findFirst()
                .orElseThrow(NotFoundException::new),
            mode, opts, user);
      default:
        throw new IllegalStateException("Unknown save mode " + mode.name());
    }
  }

  @Override
  public void save(Stream<Node> values, SaveMode mode, WriteOptions opts, User user) {
    switch (mode) {
      case INSERT:
        super.save(insert(values, opts, user), mode, opts, user);
        break;
      case UPDATE:
        super.save(pairAndUpdate(values, opts, user), mode, opts, user);
        break;
      case UPSERT:
        super.save(pairAndUpsert(values, opts, user), mode, opts, user);
        break;
      default:
        throw new IllegalStateException("Unknown save mode " + mode.name());
    }
  }

  @Override
  public void saveAndDelete(Stream<Node> saves, Stream<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    switch (mode) {
      case INSERT:
        super.saveAndDelete(insert(saves, opts, user), deletes, mode, opts, user);
        break;
      case UPDATE:
        super.saveAndDelete(pairAndUpdate(saves, opts, user), deletes, mode, opts, user);
        break;
      case UPSERT:
        super.saveAndDelete(pairAndUpsert(saves, opts, user), deletes, mode, opts, user);
        break;
      default:
        throw new IllegalStateException("Unknown save mode " + mode.name());
    }
  }

  private Stream<Tuple2<Optional<Node>, Node>> pairWithOldNodes(Stream<Node> newNodes, User user) {
    return StreamUtils.partitionedMap(newNodes, 100,
        nodeList -> {
          List<Select> selectBaseFields = ImmutableList.of(
              Selects.field("id"),
              Selects.field("type.id"),
              Selects.field("type.graph.id"),
              Selects.field("uri"),
              Selects.field("code"),
              Selects.field("number"),
              Selects.field("createdBy"),
              Selects.field("createdDate"),
              Selects.field("lastModifiedBy"),
              Selects.field("lastModifiedDate"));

          Specification<NodeId, Node> anyNodeSpec = or(nodeList.stream()
              .map(n -> and(
                  NodesByGraphId.of(n.getTypeGraphId()),
                  NodesByTypeId.of(n.getTypeId()),
                  NodesById.of(n.getId())
              )).collect(toImmutableList()));

          Query<NodeId, Node> query = new Query<>(selectBaseFields, anyNodeSpec);

          Map<NodeId, Node> oldNodes;
          try (Stream<Node> values = values(query, user)) {
            oldNodes = values.collect(toMap(Node::identifier, n -> n));
          }

          return nodeList.stream()
              .map(newNode -> Tuple.of(
                  Optional.ofNullable(oldNodes.get(newNode.identifier())),
                  newNode));
        });
  }

  private Stream<Node> pairAndUpdate(Stream<Node> nodes, WriteOptions opts, User user) {
    return update(pairWithOldNodes(nodes, user)
        .map(t -> Tuple.of(t._1.orElseThrow(NotFoundException::new), t._2)), opts, user);
  }

  private Stream<Node> pairAndUpsert(Stream<Node> nodes, WriteOptions opts, User user) {
    return upsert(pairWithOldNodes(nodes, user), opts, user);
  }

  public abstract Stream<Node> insert(Stream<Node> newNodes, WriteOptions opts, User user);

  public abstract Stream<Node> update(Stream<Tuple2<Node, Node>> oldAndNewNodes,
      WriteOptions opts, User user);

  public abstract Stream<Node> upsert(Stream<Tuple2<Optional<Node>, Node>> optionalOldAndNewNodes,
      WriteOptions opts, User user);

}
