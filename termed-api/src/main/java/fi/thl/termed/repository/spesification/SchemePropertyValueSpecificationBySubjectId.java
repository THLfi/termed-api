package fi.thl.termed.repository.spesification;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.LangValue;

public class SchemePropertyValueSpecificationBySubjectId
    extends SqlSpecification<PropertyValueId<UUID>, LangValue> {

  private UUID schemeId;

  public SchemePropertyValueSpecificationBySubjectId(UUID schemeId) {
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
