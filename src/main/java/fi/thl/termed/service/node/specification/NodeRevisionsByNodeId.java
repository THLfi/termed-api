package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeRevisionsByNodeId extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private NodeId nodeId;

  public NodeRevisionsByNodeId(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public boolean test(RevisionId<NodeId> revisionId, Tuple2<RevisionType, Node> revision) {
    return Objects.equals(nodeId, revisionId.getId());
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("graph_id = ? and type_id = ? and id = ?",
        nodeId.getTypeGraphId(), nodeId.getTypeId(), nodeId.getId());
  }

}
