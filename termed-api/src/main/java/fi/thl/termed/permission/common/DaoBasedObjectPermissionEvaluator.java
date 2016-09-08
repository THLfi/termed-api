package fi.thl.termed.permission.common;

import java.io.Serializable;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class DaoBasedObjectPermissionEvaluator<E extends Serializable>
    implements PermissionEvaluator<E> {

  private Dao<ObjectRolePermission<E>, Void> permissionDao;

  public DaoBasedObjectPermissionEvaluator(Dao<ObjectRolePermission<E>, Void> permissionDao) {
    this.permissionDao = permissionDao;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (permissionDao.exists(new ObjectRolePermission<E>(object, schemeRole, permission))) {
        return true;
      }
    }
    return false;
  }

}
