package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class ReferenceAttributesByTypeId
    extends AbstractSqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private TypeId typeId;

  public ReferenceAttributesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(ReferenceAttributeId key, ReferenceAttribute value) {
    return Objects.equals(key.getDomainId(), typeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "domain_graph_id = ? and domain_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
  }

}
