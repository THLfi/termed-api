package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.domain.LangValue;

public class ReferenceAttributePropertiesByAttributeId
    extends AbstractSpecification<PropertyValueId<ReferenceAttributeId>, LangValue>
    implements SqlSpecification<PropertyValueId<ReferenceAttributeId>, LangValue> {

  private ReferenceAttributeId referenceAttributeId;

  public ReferenceAttributePropertiesByAttributeId(
      ReferenceAttributeId referenceAttributeId) {
    this.referenceAttributeId = referenceAttributeId;
  }

  @Override
  public boolean accept(PropertyValueId<ReferenceAttributeId> propertyValueId, LangValue value) {
    return Objects.equal(propertyValueId.getSubjectId(), referenceAttributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = referenceAttributeId.getDomainId();
    return new Object[]{domainId.getSchemeId(),
                        domainId.getId(),
                        referenceAttributeId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceAttributePropertiesByAttributeId that = (ReferenceAttributePropertiesByAttributeId) o;
    return Objects.equal(referenceAttributeId, that.referenceAttributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(referenceAttributeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("referenceAttributeId", referenceAttributeId)
        .toString();
  }

}
