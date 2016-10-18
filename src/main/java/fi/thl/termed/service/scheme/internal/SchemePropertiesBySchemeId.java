package fi.thl.termed.service.scheme.internal;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemePropertiesBySchemeId
    extends AbstractSqlSpecification<PropertyValueId<UUID>, LangValue> {

  private UUID schemeId;

  public SchemePropertiesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(PropertyValueId<UUID> key, LangValue langValue) {
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

}
