package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.IndexingQueueItemId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeIndexingQueueItemsByQueueId extends
    AbstractSqlSpecification<IndexingQueueItemId<NodeId>, Empty> {

  private final Long queueId;

  private NodeIndexingQueueItemsByQueueId(Long queueId) {
    this.queueId = queueId;
  }

  public static NodeIndexingQueueItemsByQueueId of(Long queueId) {
    return new NodeIndexingQueueItemsByQueueId(queueId);
  }

  @Override
  public boolean test(IndexingQueueItemId<NodeId> queueItemId, Empty empty) {
    return Objects.equals(queueId, queueItemId.getIndexingQueueId());
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("node_indexing_queue_id = ?", queueId);
  }

}
