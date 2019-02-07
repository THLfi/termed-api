package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.UUIDs.nameUUIDFromString;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.ErrorCode;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Optional;
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
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    super.save(nodes.map(this::resolveId), mode, opts, user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(resolveId(node), mode, opts, user);
  }

  private Node resolveId(Node node) {
    if (node.getId() != null) {
      return node;
    }

    Preconditions.checkNotNull(node.getTypeGraphId(), ErrorCode.NODE_GRAPH_ID_MISSING);
    Preconditions.checkNotNull(node.getTypeId(), ErrorCode.NODE_TYPE_ID_MISSING);

    UUID graphId = node.getTypeGraphId();
    String typeId = node.getTypeId();

    Optional<String> code = node.getCode();
    Optional<String> uri = node.getUri();

    if (code.isPresent()) {
      return Node.builder()
          .id(nameUUIDFromString(graphId + "-" + typeId + "-" + code.get()), typeId, graphId)
          .copyOptionalsFrom(node)
          .build();
    }

    if (uri.isPresent()) {
      return Node.builder()
          .id(nameUUIDFromString(graphId + "-" + uri.get()), typeId, graphId)
          .copyOptionalsFrom(node)
          .build();
    }

    return Node.builder()
        .id(UUID.randomUUID(), typeId, graphId)
        .copyOptionalsFrom(node)
        .build();
  }

}
