package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static fi.thl.termed.util.service.WriteOptions.opts;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.List;

public class RevisionInitializingNodeService extends ForwardingService<NodeId, Node> {

  private SequenceService revisionSequenceService;
  private Service2<Long, Revision> revisionService;

  public RevisionInitializingNodeService(
      Service<NodeId, Node> delegate,
      SequenceService revisionSequenceService,
      Service2<Long, Revision> revisionService) {
    super(delegate);
    this.revisionSequenceService = revisionSequenceService;
    this.revisionService = revisionService;
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    return super.save(nodes, mode, opts(opts.isSync(), newRevision(user)), user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(node, mode, opts(opts.isSync(), newRevision(user)), user);
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    super.delete(id, opts(opts.isSync(), newRevision(user)), user);
  }

  @Override
  public void delete(List<NodeId> ids, WriteOptions opts, User user) {
    super.delete(ids, opts(opts.isSync(), newRevision(user)), user);
  }

  @Override
  public List<NodeId> saveAndDelete(List<Node> saves, List<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    return super.saveAndDelete(saves, deletes, mode, opts(opts.isSync(), newRevision(user)), user);
  }

  private Long newRevision(User user) {
    return revisionService.save(
        Revision.of(revisionSequenceService.getAndAdvance(user), user.getUsername(), new Date()),
        INSERT, defaultOpts(), user);
  }

}
