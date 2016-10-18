package fi.thl.termed.service.scheme.internal;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemeRolesBySchemeId extends AbstractSqlSpecification<SchemeRole, Empty> {

  private UUID schemeId;

  public SchemeRolesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(SchemeRole schemeRole, Empty value) {
    return Objects.equals(schemeRole.getSchemeId(), schemeId);
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
