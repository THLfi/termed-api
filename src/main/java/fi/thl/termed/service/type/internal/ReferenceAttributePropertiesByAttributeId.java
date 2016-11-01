package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ReferenceAttributePropertiesByAttributeId
    extends AbstractSqlSpecification<PropertyValueId<ReferenceAttributeId>, LangValue> {

  private ReferenceAttributeId referenceAttributeId;

  public ReferenceAttributePropertiesByAttributeId(ReferenceAttributeId referenceAttributeId) {
    this.referenceAttributeId = referenceAttributeId;
  }

  @Override
  public boolean test(PropertyValueId<ReferenceAttributeId> propertyValueId, LangValue value) {
    return Objects.equals(propertyValueId.getSubjectId(), referenceAttributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "reference_attribute_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    TypeId domainId = referenceAttributeId.getDomainId();
    return new Object[]{domainId.getGraphId(),
                        domainId.getId(),
                        referenceAttributeId.getId()};
  }

}
