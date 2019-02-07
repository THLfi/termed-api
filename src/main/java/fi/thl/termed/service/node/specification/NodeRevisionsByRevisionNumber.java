package fi.thl.termed.service.node.specification;

import static java.util.Objects.requireNonNull;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeRevisionsByRevisionNumber extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Long revisionNumber;

  public NodeRevisionsByRevisionNumber(Long revisionNumber) {
    this.revisionNumber = requireNonNull(revisionNumber);
  }

  public static NodeRevisionsByRevisionNumber of(Long revisionNumber) {
    return new NodeRevisionsByRevisionNumber(revisionNumber);
  }

  @Override
  public boolean test(RevisionId<NodeId> revisionId, Tuple2<RevisionType, Node> revision) {
    return Objects.equals(revisionId.getRevision(), revisionNumber);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("revision = ?", revisionNumber);
  }

}
