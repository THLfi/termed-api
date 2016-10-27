package fi.thl.termed.service.resource.internal;

import java.util.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ResourceReferenceAttributeValuesByResourceId
    extends AbstractSqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ResourceId resourceId;

  public ResourceReferenceAttributeValuesByResourceId(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean test(ResourceAttributeValueId attributeValueId, ResourceId value) {
    return Objects.equals(attributeValueId.getResourceId(), resourceId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and resource_type_id = ? and resource_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{resourceId.getTypeSchemeId(), resourceId.getTypeId(), resourceId.getId()};
  }

}
