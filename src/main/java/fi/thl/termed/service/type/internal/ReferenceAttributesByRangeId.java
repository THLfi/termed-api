package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import java.util.Objects;

public class ReferenceAttributesByRangeId
    extends AbstractSqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private TypeId typeId;

  public ReferenceAttributesByRangeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(ReferenceAttributeId key, ReferenceAttribute value) {
    return Objects.equals(key.getDomainId(), typeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "range_graph_id = ? and range_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
  }

}
