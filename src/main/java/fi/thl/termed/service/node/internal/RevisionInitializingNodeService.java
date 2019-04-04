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
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.stream.Stream;

public class RevisionInitializingNodeService extends ForwardingService<NodeId, Node> {

  private SequenceService revisionSequenceService;
  private Service<Long, Revision> revisionService;

  public RevisionInitializingNodeService(
      Service<NodeId, Node> delegate,
      SequenceService revisionSequenceService,
      Service<Long, Revision> revisionService) {
    super(delegate);
    this.revisionSequenceService = revisionSequenceService;
    this.revisionService = revisionService;
  }

  @Override
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    super.save(nodes, mode,
        opts(opts.isSync(), newRevision(user), opts.isGenerateCodes(), opts.isGenerateUris()),
        user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(node, mode,
        opts(opts.isSync(), newRevision(user), opts.isGenerateCodes(), opts.isGenerateUris()),
        user);
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    super.delete(id,
        opts(opts.isSync(), newRevision(user), opts.isGenerateCodes(), opts.isGenerateUris()),
        user);
  }

  @Override
  public void delete(Stream<NodeId> ids, WriteOptions opts, User user) {
    super.delete(ids,
        opts(opts.isSync(), newRevision(user), opts.isGenerateCodes(), opts.isGenerateUris()),
        user);
  }

  @Override
  public void saveAndDelete(Stream<Node> saves, Stream<NodeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    super.saveAndDelete(saves, deletes, mode,
        opts(opts.isSync(), newRevision(user), opts.isGenerateCodes(), opts.isGenerateUris()),
        user);
  }

  private Long newRevision(User user) {
    return revisionService.save(
        Revision.of(revisionSequenceService.getAndAdvance(user), user.getUsername(), new Date()),
        INSERT, defaultOpts(), user);
  }

}
