package fi.thl.termed.repository.spesification;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.LangValue;

public class ReferenceAttributePropertyValueSpecificationBySubjectId
    extends SqlSpecification<PropertyValueId<ReferenceAttributeId>, LangValue> {

  private ReferenceAttributeId referenceAttributeId;

  public ReferenceAttributePropertyValueSpecificationBySubjectId(
      ReferenceAttributeId referenceAttributeId) {
    this.referenceAttributeId = referenceAttributeId;
  }

  @Override
  public boolean accept(PropertyValueId<ReferenceAttributeId> propertyValueId, LangValue value) {
    return Objects.equal(propertyValueId.getSubjectId(), referenceAttributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_range_scheme_id = ? and reference_attribute_range_id = ? and reference_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = referenceAttributeId.getDomainId();
    ClassId rangeId = referenceAttributeId.getRangeId();
    return new Object[]{domainId.getSchemeId(),
                        domainId.getId(),
                        rangeId.getSchemeId(),
                        rangeId.getId(),
                        referenceAttributeId.getId()};
  }

}
