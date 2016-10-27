package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemePermissionsBySchemeId
    extends AbstractSqlSpecification<ObjectRolePermission<SchemeId>, GrantedPermission> {

  private SchemeId schemeId;

  public SchemePermissionsBySchemeId(SchemeId schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(ObjectRolePermission<SchemeId> objectRolePermission,
                      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), schemeId);
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
