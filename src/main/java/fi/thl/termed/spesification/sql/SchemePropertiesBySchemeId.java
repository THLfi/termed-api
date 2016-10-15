package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.domain.LangValue;

public class SchemePropertiesBySchemeId
    extends AbstractSpecification<PropertyValueId<UUID>, LangValue>
    implements SqlSpecification<PropertyValueId<UUID>, LangValue> {

  private UUID schemeId;

  public SchemePropertiesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(PropertyValueId<UUID> key, LangValue langValue) {
    return Objects.equals(key.getSubjectId(), schemeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemePropertiesBySchemeId that = (SchemePropertiesBySchemeId) o;
    return Objects.equals(schemeId, that.schemeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .toString();
  }

}
