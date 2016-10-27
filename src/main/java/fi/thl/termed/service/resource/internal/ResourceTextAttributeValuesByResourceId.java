package fi.thl.termed.service.resource.internal;

import java.util.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ResourceTextAttributeValuesByResourceId
    extends AbstractSqlSpecification<ResourceAttributeValueId, StrictLangValue> {

  private ResourceId resourceId;

  public ResourceTextAttributeValuesByResourceId(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean test(ResourceAttributeValueId attributeValueId, StrictLangValue langValue) {
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
