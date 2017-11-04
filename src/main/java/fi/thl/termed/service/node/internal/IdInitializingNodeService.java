package fi.thl.termed.service.node.internal;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.ErrorCode;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
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
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    nodes.forEach(this::resolveId);
    return super.save(nodes, mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    resolveId(node);
    return super.save(node, mode, opts, user);
  }

  @Override
  public List<NodeId> saveAndDelete(List<Node> saves, List<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    saves.forEach(this::resolveId);
    return super.saveAndDelete(saves, deletes, mode, opts, user);
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
