package fi.thl.termed.permission.util;

import java.util.Map;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class MapEntryPermissionEvaluator<K, V> implements PermissionEvaluator<Map.Entry<K, V>> {

  private PermissionEvaluator<K> keyEvaluator;
  private PermissionEvaluator<V> valueEvaluator;

  public MapEntryPermissionEvaluator(PermissionEvaluator<K> keyEvaluator,
                                     PermissionEvaluator<V> valueEvaluator) {
    this.keyEvaluator = keyEvaluator;
    this.valueEvaluator = valueEvaluator;
  }

  @Override
  public boolean hasPermission(User user, Map.Entry<K, V> entry, Permission permission) {
    return keyEvaluator.hasPermission(user, entry.getKey(), permission) &&
           valueEvaluator.hasPermission(user, entry.getValue(), permission);
  }

}
