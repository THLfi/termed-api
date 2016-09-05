package fi.thl.termed.permission.common;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * Accepts if any of the evaluators accepts.
 */
public class DisjunctionPermissionEvaluator<K extends Serializable, V>
    implements PermissionEvaluator<K, V> {

  private List<PermissionEvaluator<K, V>> evaluators;

  public DisjunctionPermissionEvaluator(List<PermissionEvaluator<K, V>> evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean hasPermission(User user, K key, Permission permission) {
    for (PermissionEvaluator<K, V> evaluator : evaluators) {
      if (evaluator.hasPermission(user, key, permission)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(User user, V value, Permission permission) {
    for (PermissionEvaluator<K, V> evaluator : evaluators) {
      if (evaluator.hasPermission(user, value, permission)) {
        return true;
      }
    }

    return false;
  }

}
