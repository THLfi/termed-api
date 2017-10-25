package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.service.WriteOptions.opts;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;

public class RevisionInitializingNodeService extends ForwardingService<NodeId, Node> {

  private SequenceService revSeqService;

  public RevisionInitializingNodeService(Service<NodeId, Node> delegate,
      SequenceService revisionSequenceService) {
    super(delegate);
    this.revSeqService = revisionSequenceService;
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    return super.save(nodes, mode, opts(opts.isSync(), revSeqService.getAndAdvance(user)), user);
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    return super.save(node, mode, opts(opts.isSync(), revSeqService.getAndAdvance(user)), user);
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    super.delete(id, opts(opts.isSync(), revSeqService.getAndAdvance(user)), user);
  }

  @Override
  public void delete(List<NodeId> ids, WriteOptions opts, User user) {
    super.delete(ids, opts(opts.isSync(), revSeqService.getAndAdvance(user)), user);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves, SaveMode mode,
      WriteOptions opts, User user) {
    return super.deleteAndSave(deletes, saves, mode,
        opts(opts.isSync(), revSeqService.getAndAdvance(user)), user);
  }

}
