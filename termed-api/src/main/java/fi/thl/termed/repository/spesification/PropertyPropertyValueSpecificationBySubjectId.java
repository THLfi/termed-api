package fi.thl.termed.repository.spesification;

import com.google.common.base.Objects;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.LangValue;

public class PropertyPropertyValueSpecificationBySubjectId
    extends SqlSpecification<PropertyValueId<String>, LangValue> {

  private String propertyId;

  public PropertyPropertyValueSpecificationBySubjectId(String propertyId) {
    this.propertyId = propertyId;
  }

  @Override
  public boolean accept(PropertyValueId<String> propertyValueId, LangValue langValue) {
    return Objects.equal(propertyValueId.getSubjectId(), propertyId);
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
