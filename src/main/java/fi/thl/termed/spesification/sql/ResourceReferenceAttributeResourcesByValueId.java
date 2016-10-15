package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class ResourceReferenceAttributeResourcesByValueId
    extends AbstractSpecification<ResourceAttributeValueId, ResourceId>
    implements SqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ResourceId valueId;

  public ResourceReferenceAttributeResourcesByValueId(ResourceId valueId) {
    this.valueId = valueId;
  }

  @Override
  public boolean accept(ResourceAttributeValueId key, ResourceId value) {
    return Objects.equal(value, valueId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "value_scheme_id = ? and value_type_id = ? and value_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{valueId.getSchemeId(), valueId.getTypeId(), valueId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceReferenceAttributeResourcesByValueId that =
        (ResourceReferenceAttributeResourcesByValueId) o;
    return Objects.equal(valueId, that.valueId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(valueId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("valueId", valueId)
        .toString();
  }

}
