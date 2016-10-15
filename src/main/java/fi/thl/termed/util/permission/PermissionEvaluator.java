package fi.thl.termed.util.permission;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;

public interface PermissionEvaluator<E> {

  boolean hasPermission(User user, E object, Permission permission);

}
