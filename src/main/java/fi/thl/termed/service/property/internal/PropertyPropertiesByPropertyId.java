package fi.thl.termed.service.property.internal;

import java.util.Objects;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class PropertyPropertiesByPropertyId
    extends AbstractSqlSpecification<PropertyValueId<String>, LangValue> {

  private String propertyId;

  public PropertyPropertiesByPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  @Override
  public boolean test(PropertyValueId<String> propertyValueId, LangValue langValue) {
    return Objects.equals(propertyValueId.getSubjectId(), propertyId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "subject_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{propertyId};
  }

}
