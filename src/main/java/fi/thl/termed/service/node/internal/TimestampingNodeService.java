package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.query.AndSpecification.and;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.service.ForwardingService2;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

public class TimestampingNodeService extends ForwardingService2<NodeId, Node> {

  public TimestampingNodeService(Service2<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Date now = new Date();
    return super.save(nodes.map(n -> addTimestamp(n, user, now)), mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(addTimestamp(node, user, new Date()), mode, opts, user);
  }

  private Node addTimestamp(Node node, User user, Date now) {
    try (Stream<Node> indexedNodes = values(new Query<>(and(
        new NodesByGraphId(node.getTypeGraphId()),
        new NodesByTypeId(node.getTypeId()),
        new NodeById(node.getId()))), user)) {

      Optional<Node> indexedNode = indexedNodes.findAny();

      if (indexedNode.isPresent()) {
        node.setCreatedDate(indexedNode.get().getCreatedDate());
        node.setCreatedBy(indexedNode.get().getCreatedBy());
      } else {
        node.setCreatedDate(now);
        node.setCreatedBy(user.getUsername());
      }

      node.setLastModifiedDate(now);
      node.setLastModifiedBy(user.getUsername());

      return node;
    }
  }

}
