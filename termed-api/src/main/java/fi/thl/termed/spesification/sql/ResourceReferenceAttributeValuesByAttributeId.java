package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.SqlSpecification;

public class ResourceReferenceAttributeValuesByAttributeId
    extends AbstractSpecification<ResourceAttributeValueId, ResourceId>
    implements SqlSpecification<ResourceAttributeValueId, ResourceId> {

  private ReferenceAttributeId attributeId;

  public ResourceReferenceAttributeValuesByAttributeId(ReferenceAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  public ReferenceAttributeId getAttributeId() {
    return attributeId;
  }

  @Override
  protected boolean accept(ResourceAttributeValueId attributeValueId, ResourceId value) {
    ReferenceAttributeId valueAttributeId =
        new ReferenceAttributeId(new ClassId(attributeValueId.getResourceId()),
                                 attributeValueId.getAttributeId());
    return Objects.equal(valueAttributeId, attributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and resource_type_id = ? and attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = attributeId.getDomainId();
    return new Object[]{domainId.getSchemeId(), domainId.getId(), attributeId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceReferenceAttributeValuesByAttributeId that =
        (ResourceReferenceAttributeValuesByAttributeId) o;
    return Objects.equal(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(attributeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .toString();
  }

}
