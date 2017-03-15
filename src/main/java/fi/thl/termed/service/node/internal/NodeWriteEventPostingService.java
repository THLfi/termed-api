package fi.thl.termed.service.node.internal;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.NodeDeletedEvent;
import fi.thl.termed.domain.event.NodeSavedEvent;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Specification;
import java.util.List;
import java.util.Optional;

/**
 * Posts events to event bus for each node save and delete request.
 */
public class NodeWriteEventPostingService implements Service<NodeId, Node> {

  private Service<NodeId, Node> delegate;
  private EventBus eventBus;

  public NodeWriteEventPostingService(Service<NodeId, Node> delegate, EventBus eventBus) {
    this.delegate = delegate;
    this.eventBus = eventBus;
  }

  @Override
  public List<NodeId> save(List<Node> nodes, User user) {
    List<NodeId> ids = delegate.save(nodes, user);
    ids.forEach(id -> eventBus.post(new NodeSavedEvent(id, user.getUsername())));
    return ids;
  }

  @Override
  public NodeId save(Node node, User user) {
    NodeId id = delegate.save(node, user);
    eventBus.post(new NodeSavedEvent(id, user.getUsername()));
    return id;
  }

  @Override
  public void delete(List<NodeId> ids, User user) {
    delegate.delete(ids, user);
    ids.forEach(id -> eventBus.post(new NodeDeletedEvent(id, user.getUsername())));
  }

  @Override
  public void delete(NodeId id, User user) {
    delegate.delete(id, user);
    eventBus.post(new NodeDeletedEvent(id, user.getUsername()));
  }

  @Override
  public List<Node> get(Specification<NodeId, Node> s, List<String> sort, int max, User u) {
    return delegate.get(s, sort, max, u);
  }

  @Override
  public List<NodeId> getKeys(Specification<NodeId, Node> s, List<String> sort, int max, User u) {
    return delegate.getKeys(s, sort, max, u);
  }

  @Override
  public List<Node> get(List<NodeId> ids, User user) {
    return delegate.get(ids, user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user) {
    return delegate.get(id, user);
  }

}
