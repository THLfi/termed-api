package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;

public class NodeRevisionsLessOrEqualToRevisionNumber extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Long revision;

  public NodeRevisionsLessOrEqualToRevisionNumber(Long revision) {
    this.revision = revision;
  }

  public static NodeRevisionsLessOrEqualToRevisionNumber of(Long revision) {
    return new NodeRevisionsLessOrEqualToRevisionNumber(revision);
  }

  @Override
  public boolean test(RevisionId<NodeId> key, Tuple2<RevisionType, Node> value) {
    return key.getRevision() <= revision;
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("revision <= ?", revision);
  }

}
