package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.ObjectUtils.castBoolean;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.NodeDeletedEvent;
import fi.thl.termed.domain.event.NodeSavedEvent;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Specification;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
  public List<NodeId> save(List<Node> values, Map<String, Object> args, User user) {
    List<NodeId> ids = delegate.save(values, args, user);
    fireSaveEvents(ids, user.getUsername(), castBoolean(args.get("sync"), false));
    return ids;
  }

  private void fireSaveEvents(List<NodeId> ids, String user, boolean sync) {
    Date date = new Date();
    ids.forEach(id -> eventBus.post(new NodeSavedEvent(user, date, sync, id)));
  }

  @Override
  public NodeId save(Node value, Map<String, Object> args, User user) {
    NodeId id = delegate.save(value, args, user);
    fireSaveEvent(id, user.getUsername(), castBoolean(args.get("sync"), false));
    return id;
  }

  private void fireSaveEvent(NodeId id, String user, boolean sync) {
    eventBus.post(new NodeSavedEvent(user, new Date(), sync, id));
  }

  @Override
  public void delete(List<NodeId> ids, Map<String, Object> args, User user) {
    delegate.delete(ids, args, user);
    fireDeleteEvents(ids, user.getUsername(), castBoolean(args.get("sync"), false));
  }

  private void fireDeleteEvents(List<NodeId> ids, String user, boolean sync) {
    Date date = new Date();
    ids.forEach(id -> eventBus.post(new NodeDeletedEvent(user, date, sync, id)));
  }

  @Override
  public void delete(NodeId id, Map<String, Object> args, User user) {
    delegate.delete(id, args, user);
    fireDeleteEvent(id, user.getUsername(), castBoolean(args.get("sync"), false));
  }

  private void fireDeleteEvent(NodeId id, String user, boolean sync) {
    eventBus.post(new NodeDeletedEvent(user, new Date(), sync, id));
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves,
      Map<String, Object> args, User user) {
    List<NodeId> ids = delegate.deleteAndSave(deletes, saves, args, user);
    fireDeleteEvents(deletes, user.getUsername(), castBoolean(args.get("sync"), false));
    fireSaveEvents(ids, user.getUsername(), castBoolean(args.get("sync"), false));
    return ids;
  }

  @Override
  public Stream<Node> get(Specification<NodeId, Node> spec, Map<String, Object> args, User user) {
    return delegate.get(spec, args, user);
  }

  @Override
  public Stream<NodeId> getKeys(Specification<NodeId, Node> spec, Map<String, Object> args,
      User user) {
    return delegate.getKeys(spec, args, user);
  }

  @Override
  public Stream<Node> get(List<NodeId> ids, Map<String, Object> args, User user) {
    return delegate.get(ids, args, user);
  }

  @Override
  public Optional<Node> get(NodeId id, Map<String, Object> args, User user) {
    return delegate.get(id, args, user);
  }

}
