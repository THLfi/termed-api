package fi.thl.termed.service.node.internal;

import static com.google.common.base.MoreObjects.firstNonNull;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.Map;

public class AttributeValueInitializingNodeService
    extends ForwardingService<NodeId, Node> {

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public List<NodeId> save(List<Node> nodes, Map<String, Object> args, User currentUser) {
    nodes.forEach(this::resolveAttributes);
    return super.save(nodes, args, currentUser);
  }

  @Override
  public NodeId save(Node node, Map<String, Object> args, User currentUser) {
    resolveAttributes(node);
    return super.save(node, args, currentUser);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves,
      Map<String, Object> args, User user) {
    saves.forEach(this::resolveAttributes);
    return super.deleteAndSave(deletes, saves, args, user);
  }

  private void resolveAttributes(Node node) {
    for (StrictLangValue value : node.getProperties().values()) {
      value.setRegex(firstNonNull(value.getRegex(), RegularExpressions.ALL));
    }
    for (NodeId value : node.getReferences().values()) {
      value.setType(firstNonNull(value.getType(), node.getType()));
    }
  }

}
