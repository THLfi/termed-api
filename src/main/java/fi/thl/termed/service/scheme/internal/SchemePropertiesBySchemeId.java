package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemePropertiesBySchemeId
    extends AbstractSqlSpecification<PropertyValueId<SchemeId>, LangValue> {

  private SchemeId schemeId;

  public SchemePropertiesBySchemeId(SchemeId schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(PropertyValueId<SchemeId> key, LangValue langValue) {
    return Objects.equals(key.getSubjectId(), schemeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId.getId()};
  }

}
