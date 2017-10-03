package fi.thl.termed.service.property.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class PropertyPropertiesByPropertyId
    extends AbstractSqlSpecification<PropertyValueId<String>, LangValue> {

  private String propertyId;

  PropertyPropertiesByPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  @Override
  public boolean test(PropertyValueId<String> propertyValueId, LangValue langValue) {
    return Objects.equals(propertyValueId.getSubjectId(), propertyId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("subject_id = ?", propertyId);
  }

}
