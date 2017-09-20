package fi.thl.termed.service.node.internal;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.ErrorCode;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.UUID;

/**
 * Make sure that node has an identifier
 */
public class IdInitializingNodeService extends ForwardingService<NodeId, Node> {

  public IdInitializingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public List<NodeId> save(List<Node> nodes, User user, Arg... args) {
    nodes.forEach(this::resolveId);
    return super.save(nodes, user, args);
  }

  @Override
  public NodeId save(Node node, User user, Arg... args) {
    resolveId(node);
    return super.save(node, user, args);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves, User user,
      Arg... args) {
    saves.forEach(this::resolveId);
    return super.deleteAndSave(deletes, saves, user, args);
  }

  private void resolveId(Node node) {
    Preconditions.checkNotNull(node.getTypeGraphId(), ErrorCode.NODE_GRAPH_ID_MISSING);
    Preconditions.checkNotNull(node.getTypeId(), ErrorCode.NODE_TYPE_ID_MISSING);

    UUID graphId = node.getTypeGraphId();
    String typeId = node.getTypeId();
    UUID id = node.getId();

    String code = node.getCode();
    String uri = node.getUri();

    if (id == null && code != null) {
      id = UUIDs.nameUUIDFromString(graphId + "-" + typeId + "-" + code);
    }
    if (id == null && uri != null) {
      id = UUIDs.nameUUIDFromString(graphId + "-" + uri);
    }
    if (id == null) {
      id = UUID.randomUUID();
    }

    node.setId(id);
  }

}
