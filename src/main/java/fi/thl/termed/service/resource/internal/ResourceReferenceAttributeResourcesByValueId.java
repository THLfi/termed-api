package fi.thl.termed.service.resource.internal;

import java.util.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ResourceReferenceAttributeResourcesByValueId
    extends AbstractSqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ResourceId valueId;

  public ResourceReferenceAttributeResourcesByValueId(ResourceId valueId) {
    this.valueId = valueId;
  }

  @Override
  public boolean test(ResourceAttributeValueId key, ResourceId value) {
    return Objects.equals(value, valueId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "value_scheme_id = ? and value_type_id = ? and value_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{valueId.getTypeSchemeId(), valueId.getTypeId(), valueId.getId()};
  }

}
