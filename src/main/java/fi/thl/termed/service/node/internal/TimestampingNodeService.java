package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.query.AndSpecification.and;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesById;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

public class TimestampingNodeService extends ForwardingService<NodeId, Node> {

  public TimestampingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    LocalDateTime now = LocalDateTime.now();
    super.save(nodes.map(n -> addTimestamp(n, user, now)), mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(addTimestamp(node, user, LocalDateTime.now()), mode, opts, user);
  }

  @Override
  public void saveAndDelete(Stream<Node> saves, Stream<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    LocalDateTime now = LocalDateTime.now();
    super.saveAndDelete(saves.map(n -> addTimestamp(n, user, now)), deletes, mode, opts, user);
  }

  private Node addTimestamp(Node node, User user, LocalDateTime now) {
    try (Stream<Node> indexedNodes = values(new Query<>(and(
        new NodesByGraphId(node.getTypeGraphId()),
        new NodesByTypeId(node.getTypeId()),
        new NodesById(node.getId()))), user)) {

      Optional<Node> indexedNode = indexedNodes.findAny();

      Node.Builder builder = Node.builderFromCopyOf(node);

      if (indexedNode.isPresent()) {
        builder.createdDate(indexedNode.get().getCreatedDate());
        builder.createdBy(indexedNode.get().getCreatedBy());
      } else {
        builder.createdDate(now);
        builder.createdBy(user.getUsername());
      }

      builder.lastModifiedDate(now);
      builder.lastModifiedBy(user.getUsername());

      return builder.build();
    }
  }

}
