package fi.thl.termed.permission.common;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class PermitAllPermissionEvaluator<E> implements PermissionEvaluator<E> {

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    return true;
  }

}
