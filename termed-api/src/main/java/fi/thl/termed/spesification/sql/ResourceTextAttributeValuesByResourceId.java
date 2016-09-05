package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;
import fi.thl.termed.util.StrictLangValue;

public class ResourceTextAttributeValuesByResourceId
    extends AbstractSpecification<ResourceAttributeValueId, StrictLangValue>
    implements SqlSpecification<ResourceAttributeValueId, StrictLangValue> {

  private ResourceId resourceId;

  public ResourceTextAttributeValuesByResourceId(ResourceId resourceId) {
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
