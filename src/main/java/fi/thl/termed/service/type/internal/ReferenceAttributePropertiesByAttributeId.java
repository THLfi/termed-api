package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class ReferenceAttributePropertiesByAttributeId
    extends AbstractSqlSpecification<PropertyValueId<ReferenceAttributeId>, LangValue> {

  private ReferenceAttributeId referenceAttributeId;

  ReferenceAttributePropertiesByAttributeId(ReferenceAttributeId referenceAttributeId) {
    this.referenceAttributeId = referenceAttributeId;
  }

  @Override
  public boolean test(PropertyValueId<ReferenceAttributeId> propertyValueId, LangValue value) {
    return Objects.equals(propertyValueId.getSubjectId(), referenceAttributeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    TypeId domainId = referenceAttributeId.getDomainId();
    return ParametrizedSqlQuery.of(
        "reference_attribute_domain_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ?",
        domainId.getGraphId(), domainId.getId(), referenceAttributeId.getId());
  }

}
