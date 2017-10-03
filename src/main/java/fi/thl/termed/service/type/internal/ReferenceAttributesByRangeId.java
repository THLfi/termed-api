package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class ReferenceAttributesByRangeId
    extends AbstractSqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private TypeId typeId;

  ReferenceAttributesByRangeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(ReferenceAttributeId key, ReferenceAttribute value) {
    return Objects.equals(key.getDomainId(), typeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of(
        "range_graph_id = ? and range_id = ?", typeId.getGraphId(), typeId.getId());
  }

}
