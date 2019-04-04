package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeRevisionsByRevisionNumberAndType extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Long revisionNumber;
  private RevisionType revisionType;

  public NodeRevisionsByRevisionNumberAndType(Long revisionNumber,
      RevisionType revisionType) {
    this.revisionNumber = revisionNumber;
    this.revisionType = revisionType;
  }

  public static NodeRevisionsByRevisionNumberAndType of(Long revisionNumber,
      RevisionType revisionType) {
    return new NodeRevisionsByRevisionNumberAndType(revisionNumber, revisionType);
  }

  @Override
  public boolean test(RevisionId<NodeId> revisionId, Tuple2<RevisionType, Node> revision) {
    return Objects.equals(revisionId.getRevision(), revisionNumber) &&
        Objects.equals(revision._1, revisionType);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("revision = ? and revision_type = ?",
        revisionNumber, revisionType.toString());
  }

}
