package fi.thl.termed.permission.common;

import com.google.common.collect.SetMultimap;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * Evaluates permission purely by object identity, i.e. user is ignored.
 */
public class ObjectPermissionEvaluator<E> implements PermissionEvaluator<E> {

  private SetMultimap<E, Permission> permissions;

  public ObjectPermissionEvaluator(SetMultimap<E, Permission> permissions) {
    this.permissions = permissions;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    return permissions.get(object).contains(permission);
  }

}
