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
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Make sure that node has an identifier
 */
public class IdInitializingNodeService extends ForwardingService<NodeId, Node> {

  public IdInitializingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    return super.save(nodes.map(this::resolveId), mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(resolveId(node), mode, opts, user);
  }

  private Node resolveId(Node node) {
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

    return node;
  }

}
