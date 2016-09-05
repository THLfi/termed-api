package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;
import fi.thl.termed.util.LangValue;

public class SchemePropertiesBySchemeId
    extends AbstractSpecification<PropertyValueId<UUID>, LangValue>
    implements SqlSpecification<PropertyValueId<UUID>, LangValue> {

  private UUID schemeId;

  public SchemePropertiesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(PropertyValueId<UUID> key, LangValue langValue) {
    return Objects.equal(key.getSubjectId(), schemeId);
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
