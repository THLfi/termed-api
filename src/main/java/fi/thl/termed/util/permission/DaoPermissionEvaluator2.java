package fi.thl.termed.util.permission;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.SystemDao2;
import java.io.Serializable;

public class DaoPermissionEvaluator2<E extends Serializable> implements PermissionEvaluator<E> {

  private SystemDao2<ObjectRolePermission<E>, GrantedPermission> permissionDao;

  public DaoPermissionEvaluator2(
      SystemDao2<ObjectRolePermission<E>, GrantedPermission> permissionDao) {
    this.permissionDao = permissionDao;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    for (GraphRole graphRole : user.getGraphRoles()) {
      if (permissionDao.exists(new ObjectRolePermission<>(object, graphRole, permission))) {
        return true;
      }
    }
    return false;
  }

}
