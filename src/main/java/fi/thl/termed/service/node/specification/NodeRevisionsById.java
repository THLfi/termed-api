package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;
import java.util.UUID;

public class NodeRevisionsById extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private UUID id;

  public NodeRevisionsById(UUID id) {
    this.id = id;
  }

  public static NodeRevisionsById of(UUID id) {
    return new NodeRevisionsById(id);
  }

  @Override
  public boolean test(RevisionId<NodeId> revisionId, Tuple2<RevisionType, Node> revision) {
    return Objects.equals(id, revisionId.getId().getId());
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("id = ?", id);
  }

}
