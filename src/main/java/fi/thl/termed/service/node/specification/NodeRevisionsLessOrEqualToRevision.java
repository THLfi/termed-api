package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeRevisionsLessOrEqualToRevision extends
    AbstractSqlSpecification<RevisionId<NodeId>, Revision<NodeId, Node>> {

  private NodeId nodeId;
  private Long revision;

  public NodeRevisionsLessOrEqualToRevision(RevisionId<NodeId> revisionId) {
    this.nodeId = revisionId.getId();
    this.revision = revisionId.getRevision();
  }

  @Override
  public boolean test(RevisionId<NodeId> key, Revision<NodeId, Node> value) {
    return Objects.equals(key.getId(), nodeId) && key.getRevision() <= revision;
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of(
        "graph_id = ? and type_id = ? and id = ? and revision <= ?",
        nodeId.getTypeGraphId(), nodeId.getTypeId(), nodeId.getId(), revision);
  }

}
