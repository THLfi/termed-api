package fi.thl.termed.service.node.specification;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeRevisionsByTypeId extends
    AbstractSqlSpecification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private String typeId;

  public NodeRevisionsByTypeId(String typeId) {
    this.typeId = typeId;
  }

  public static NodeRevisionsByTypeId of(String typeId) {
    return new NodeRevisionsByTypeId(typeId);
  }

  @Override
  public boolean test(RevisionId<NodeId> revisionId, Tuple2<RevisionType, Node> revision) {
    return Objects.equals(typeId, revisionId.getId().getTypeId());
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("type_id = ?", typeId);
  }

}
