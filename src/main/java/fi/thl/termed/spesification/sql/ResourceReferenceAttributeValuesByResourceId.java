package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class ResourceReferenceAttributeValuesByResourceId
    extends AbstractSpecification<ResourceAttributeValueId, ResourceId>
    implements SqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ResourceId resourceId;

  public ResourceReferenceAttributeValuesByResourceId(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public boolean accept(ResourceAttributeValueId attributeValueId, ResourceId value) {
    return Objects.equals(attributeValueId.getResourceId(), resourceId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and resource_type_id = ? and resource_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{resourceId.getSchemeId(), resourceId.getTypeId(), resourceId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceReferenceAttributeValuesByResourceId that =
        (ResourceReferenceAttributeValuesByResourceId) o;
    return Objects.equals(resourceId, that.resourceId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(resourceId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("resourceId", resourceId)
        .toString();
  }

}
