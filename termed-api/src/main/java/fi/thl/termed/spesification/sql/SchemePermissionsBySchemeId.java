package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.ObjectRolePermission;

public class SchemePermissionsBySchemeId
    extends SqlSpecification<ObjectRolePermission<UUID>, Void> {

  private UUID schemeId;

  public SchemePermissionsBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(ObjectRolePermission<UUID> objectRolePermission, Void value) {
    return Objects.equal(objectRolePermission.getObjectId(), schemeId);
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
