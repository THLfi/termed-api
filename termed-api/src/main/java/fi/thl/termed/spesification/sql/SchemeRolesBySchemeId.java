package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.SchemeRole;

public class SchemeRolesBySchemeId extends SqlSpecification<SchemeRole, Void> {

  private UUID schemeId;

  public SchemeRolesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(SchemeRole schemeRole, Void value) {
    return Objects.equal(schemeRole.getSchemeId(), schemeId);
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
