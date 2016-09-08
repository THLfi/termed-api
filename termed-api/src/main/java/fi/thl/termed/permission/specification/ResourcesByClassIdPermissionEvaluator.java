package fi.thl.termed.permission.specification;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.resource.ResourcesByClassId;

public class ResourcesByClassIdPermissionEvaluator
    implements PermissionEvaluator<ResourcesByClassId> {

  private Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao;

  public ResourcesByClassIdPermissionEvaluator(
      Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao) {
    this.classPermissionDao = classPermissionDao;
  }

  public boolean hasPermission(User user, ResourcesByClassId specification, Permission permission) {
    ClassId classId = specification.getClassId();

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (classPermissionDao.exists(
          new ObjectRolePermission<ClassId>(classId, schemeRole, permission))) {
        return true;
      }
    }

    return false;
  }

}
