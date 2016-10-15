package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.LangValue;

public class PropertyPropertiesByPropertyId
    extends AbstractSpecification<PropertyValueId<String>, LangValue>
    implements SqlSpecification<PropertyValueId<String>, LangValue> {

  private String propertyId;

  public PropertyPropertiesByPropertyId(String propertyId) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PropertyPropertiesByPropertyId that = (PropertyPropertiesByPropertyId) o;
    return Objects.equal(propertyId, that.propertyId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(propertyId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("propertyId", propertyId)
        .toString();
  }

}
