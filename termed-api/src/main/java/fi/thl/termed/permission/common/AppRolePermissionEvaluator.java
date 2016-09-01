package fi.thl.termed.permission.common;

import com.google.common.collect.Multimap;

import java.io.Serializable;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * Evaluates permission purely by users app role, i.e. actual object is ignored.
 */
public class AppRolePermissionEvaluator<K extends Serializable, V>
    implements PermissionEvaluator<K, V> {

  private Multimap<AppRole, Permission> rolePermissions;

  public AppRolePermissionEvaluator(Multimap<AppRole, Permission> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }

  @Override
  public boolean hasPermission(User user, K key, Permission permission) {
    return rolePermissions.get(user.getAppRole()).contains(permission);
  }

  @Override
  public boolean hasPermission(User user, V value, Permission permission) {
    return rolePermissions.get(user.getAppRole()).contains(permission);
  }

}
