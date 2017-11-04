package fi.thl.termed.service.node.internal;

import static java.util.Collections.singletonList;

import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.NodeDeletedEvent;
import fi.thl.termed.domain.event.NodeSavedEvent;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.List;
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

  private void fireSaveEvents(List<NodeId> ids, String user, boolean sync) {
    eventBus.post(new NodeSavedEvent(user, new Date(), sync, ids));
  }

  private void fireSaveEvent(NodeId id, String user, boolean sync) {
    eventBus.post(new NodeSavedEvent(user, new Date(), sync, singletonList(id)));
  }

  private void fireDeleteEvents(List<NodeId> ids, String user, boolean sync) {
    eventBus.post(new NodeDeletedEvent(user, new Date(), sync, ids));
  }

  private void fireDeleteEvent(NodeId id, String user, boolean sync) {
    eventBus.post(new NodeDeletedEvent(user, new Date(), sync, singletonList(id)));
  }

  @Override
  public List<NodeId> save(List<Node> values, SaveMode mode, WriteOptions opts, User user) {
    List<NodeId> ids = delegate.save(values, mode, opts, user);
    fireSaveEvents(ids, user.getUsername(), opts.isSync());
    return ids;
  }

  @Override
  public NodeId save(Node value, SaveMode mode, WriteOptions opts, User user) {
    NodeId id = delegate.save(value, mode, opts, user);
    fireSaveEvent(id, user.getUsername(), opts.isSync());
    return id;
  }

  @Override
  public void delete(List<NodeId> ids, WriteOptions opts, User user) {
    delegate.delete(ids, opts, user);
    fireDeleteEvents(ids, user.getUsername(), opts.isSync());
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    delegate.delete(id, opts, user);
    fireDeleteEvent(id, user.getUsername(), opts.isSync());
  }

  @Override
  public List<NodeId> saveAndDelete(List<Node> saves, List<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    List<NodeId> ids = delegate.saveAndDelete(saves, deletes, mode, opts, user);
    fireSaveEvents(ids, user.getUsername(), opts.isSync());
    fireDeleteEvents(deletes, user.getUsername(), opts.isSync());
    return ids;
  }

  @Override
  public Stream<Node> getValueStream(User user) {
    return delegate.getValueStream(user);
  }

  @Override
  public Stream<Node> getValueStream(Query<NodeId, Node> query, User user) {
    return delegate.getValueStream(query, user);
  }

  @Override
  public Stream<NodeId> getKeyStream(User user) {
    return delegate.getKeyStream(user);
  }

  @Override
  public Stream<NodeId> getKeyStream(Query<NodeId, Node> query, User user) {
    return delegate.getKeyStream(query, user);
  }

  @Override
  public long count(Specification<NodeId, Node> spec, User user) {
    return delegate.count(spec, user);
  }

  @Override
  public boolean exists(NodeId key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user, Select... selects) {
    return delegate.get(id, user, selects);
  }

}
