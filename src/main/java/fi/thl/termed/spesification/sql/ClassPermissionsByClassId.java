package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.util.specification.SqlSpecification;

public class ClassPermissionsByClassId
    implements SqlSpecification<ObjectRolePermission<ClassId>, GrantedPermission> {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassPermissionsByClassId that = (ClassPermissionsByClassId) o;
    return Objects.equals(classId, that.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(classId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classId", classId)
        .toString();
  }

}
