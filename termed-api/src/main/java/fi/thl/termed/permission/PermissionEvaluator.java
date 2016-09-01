package fi.thl.termed.permission;

import java.io.Serializable;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;

public interface PermissionEvaluator<K extends Serializable, V> {

  boolean hasPermission(User user, K key, Permission permission);

  boolean hasPermission(User user, V value, Permission permission);

}
