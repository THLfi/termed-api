package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.SqlSpecification;

public class ClassPermissionsByClassId
    extends AbstractSpecification<ObjectRolePermission<ClassId>, GrantedPermission>
    implements SqlSpecification<ObjectRolePermission<ClassId>, GrantedPermission> {

  private ClassId classId;

  public ClassPermissionsByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean accept(ObjectRolePermission<ClassId> objectRolePermission, GrantedPermission value) {
    return Objects.equal(objectRolePermission.getObjectId(), classId);
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
    return Objects.equal(classId, that.classId);
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
