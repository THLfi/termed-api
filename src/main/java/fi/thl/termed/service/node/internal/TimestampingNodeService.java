package fi.thl.termed.service.node.internal;

import static java.util.Collections.singletonList;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TimestampingNodeService extends ForwardingService<NodeId, Node> {

  public TimestampingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    addTimestamps(nodes, user);
    return super.save(nodes, mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    addTimestamps(singletonList(node), user);
    return super.save(node, mode, opts, user);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves, SaveMode mode,
      WriteOptions opts, User user) {
    addTimestamps(saves, user);
    return super.deleteAndSave(deletes, saves, mode, opts, user);
  }

  private void addTimestamps(List<Node> nodes, User user) {
    Date now = new Date();

    nodes.forEach(node -> {
      // use search to get node from index
      Optional<Node> old = getValues(new AndSpecification<>(
          new NodesByGraphId(node.getTypeGraphId()),
          new NodesByTypeId(node.getTypeId()),
          new NodeById(node.getId())), user).findAny();

      if (old.isPresent()) {
        node.setCreatedDate(old.get().getCreatedDate());
        node.setCreatedBy(old.get().getCreatedBy());
      } else {
        node.setCreatedDate(now);
        node.setCreatedBy(user.getUsername());
      }

      node.setLastModifiedDate(now);
      node.setLastModifiedBy(user.getUsername());
    });
  }

}
