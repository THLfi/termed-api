package fi.thl.termed.repository.spesification;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.StrictLangValue;

public class ResourceTextAttributeValueSpecificationByResourceId
    extends SqlSpecification<ResourceAttributeValueId, StrictLangValue> {

  private ResourceId resourceId;

  public ResourceTextAttributeValueSpecificationByResourceId(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean accept(ResourceAttributeValueId attributeValueId, StrictLangValue langValue) {
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
