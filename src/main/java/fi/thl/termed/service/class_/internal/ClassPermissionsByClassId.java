package fi.thl.termed.service.class_.internal;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ClassPermissionsByClassId
    extends AbstractSqlSpecification<ObjectRolePermission<ClassId>, GrantedPermission> {

  private ClassId classId;

  public ClassPermissionsByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean test(ObjectRolePermission<ClassId> objectRolePermission, GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), classId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "class_scheme_id = ? and class_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId()};
  }

}
