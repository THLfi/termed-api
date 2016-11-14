package fi.thl.termed.service.node.internal;

import java.util.List;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static com.google.common.base.MoreObjects.firstNonNull;

public class AttributeValueInitializingNodeService
    extends ForwardingService<NodeId, Node> {

  public AttributeValueInitializingNodeService(Service<NodeId, Node> delegate) {
    super(delegate);
  }

  @Override
  public List<NodeId> save(List<Node> nodes, User currentUser) {
    nodes.forEach(this::resolveAttributes);
    return super.save(nodes, currentUser);
  }

  @Override
  public NodeId save(Node node, User currentUser) {
    resolveAttributes(node);
    return super.save(node, currentUser);
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
