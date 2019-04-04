package fi.thl.termed.service.node.internal;

import static fi.thl.termed.domain.RevisionType.DELETE;
import static fi.thl.termed.domain.RevisionType.INSERT;
import static fi.thl.termed.domain.RevisionType.UPDATE;
import static fi.thl.termed.util.query.OrSpecification.or;
import static fi.thl.termed.util.query.Queries.query;
import static java.util.Collections.singletonList;

import com.google.common.collect.Iterators;
import com.google.common.eventbus.EventBus;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.NodeDeletedEvent;
import fi.thl.termed.domain.event.NodeSavedEvent;
import fi.thl.termed.service.node.specification.NodeRevisionsByRevisionNumber;
import fi.thl.termed.service.node.specification.NodeRevisionsByRevisionNumberAndType;
import fi.thl.termed.util.collect.Tuple2;
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
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService;
  private EventBus eventBus;

  public NodeWriteEventPostingService(
      Service<NodeId, Node> delegate,
      Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService,
      EventBus eventBus) {
    this.delegate = delegate;
    this.nodeRevisionService = nodeRevisionService;
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
  public void save(Stream<Node> values, SaveMode mode, WriteOptions opts, User user) {
    delegate.save(values, mode, opts, user);

    Long revisionNumber = opts.getRevision()
        .orElseThrow(() -> new IllegalStateException("Revision not initialized"));

    try (Stream<NodeId> idsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumber.of(revisionNumber)), user)
        .map(RevisionId::getId)) {

      Iterators.partition(idsInRevision.iterator(), 1000).forEachRemaining(
          ids -> fireSaveEvents(ids, user.getUsername(), opts.isSync()));
    }
  }

  @Override
  public NodeId save(Node value, SaveMode mode, WriteOptions opts, User user) {
    NodeId id = delegate.save(value, mode, opts, user);
    fireSaveEvent(id, user.getUsername(), opts.isSync());
    return id;
  }

  @Override
  public void delete(Stream<NodeId> ids, WriteOptions opts, User user) {
    delegate.delete(ids, opts, user);

    Long revisionNumber = opts.getRevision()
        .orElseThrow(() -> new IllegalStateException("Revision not initialized"));

    try (Stream<NodeId> idsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumber.of(revisionNumber)), user)
        .map(RevisionId::getId)) {

      Iterators.partition(idsInRevision.iterator(), 1000).forEachRemaining(
          batch -> fireDeleteEvents(batch, user.getUsername(), opts.isSync()));
    }
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    delegate.delete(id, opts, user);
    fireDeleteEvent(id, user.getUsername(), opts.isSync());
  }

  @Override
  public void saveAndDelete(Stream<Node> saves, Stream<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    delegate.saveAndDelete(saves, deletes, mode, opts, user);

    Long revisionNumber = opts.getRevision()
        .orElseThrow(() -> new IllegalStateException("Revision not initialized"));

    try (Stream<NodeId> savedIdsInRevision = nodeRevisionService
        .keys(query(or(
            NodeRevisionsByRevisionNumberAndType.of(revisionNumber, INSERT),
            NodeRevisionsByRevisionNumberAndType.of(revisionNumber, UPDATE))), user)
        .map(RevisionId::getId)) {

      Iterators.partition(savedIdsInRevision.iterator(), 1000).forEachRemaining(
          batch -> fireDeleteEvents(batch, user.getUsername(), opts.isSync()));
    }

    try (Stream<NodeId> deletedIdsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumberAndType.of(revisionNumber, DELETE)), user)
        .map(RevisionId::getId)) {

      Iterators.partition(deletedIdsInRevision.iterator(), 1000).forEachRemaining(
          batch -> fireDeleteEvents(batch, user.getUsername(), opts.isSync()));
    }
  }

  @Override
  public Stream<Node> values(Query<NodeId, Node> query, User user) {
    return delegate.values(query, user);
  }

  @Override
  public Stream<NodeId> keys(Query<NodeId, Node> query, User user) {
    return delegate.keys(query, user);
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
