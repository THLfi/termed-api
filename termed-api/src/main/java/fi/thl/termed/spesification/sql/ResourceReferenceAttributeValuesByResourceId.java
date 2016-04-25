package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ResourceReferenceAttributeValuesByResourceId
    extends SqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ResourceId resourceId;

  public ResourceReferenceAttributeValuesByResourceId(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean accept(ResourceAttributeValueId attributeValueId, ResourceId value) {
    return Objects.equal(attributeValueId.getResourceId(), resourceId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and resource_type_id = ? and resource_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{resourceId.getSchemeId(), resourceId.getTypeId(), resourceId.getId()};
  }

}
