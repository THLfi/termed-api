package fi.thl.termed.permission.common;

import com.google.common.base.Function;
import com.google.common.collect.SetMultimap;

import java.io.Serializable;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * Evaluates permission purely by object identity, i.e. user is ignored.
 */
public class ObjectIdPermissionEvaluator<K extends Serializable, V>
    implements PermissionEvaluator<K, V> {

  private SetMultimap<K, Permission> permissions;
  private Function<V, K> keyFunction;

  public ObjectIdPermissionEvaluator(SetMultimap<K, Permission> permissions,
                                     Function<V, K> keyFunction) {
    this.permissions = permissions;
    this.keyFunction = keyFunction;
  }

  @Override
  public boolean hasPermission(User user, K key, Permission permission) {
    return permissions.get(key).contains(permission);
  }

  @Override
  public boolean hasPermission(User user, V value, Permission permission) {
    return hasPermission(user, keyFunction.apply(value), permission);
  }

}
