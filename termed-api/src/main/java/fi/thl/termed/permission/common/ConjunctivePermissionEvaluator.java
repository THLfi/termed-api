package fi.thl.termed.permission.common;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * Accepts if all evaluators accept. Rejects if any of the evaluators denies permission.
 */
public class ConjunctivePermissionEvaluator<K extends Serializable, V>
    implements PermissionEvaluator<K, V> {

  private List<PermissionEvaluator<K, V>> evaluators;

  public ConjunctivePermissionEvaluator(List<PermissionEvaluator<K, V>> evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean hasPermission(User user, K key, Permission permission) {
    for (PermissionEvaluator<K, V> evaluator : evaluators) {
      if (!evaluator.hasPermission(user, key, permission)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean hasPermission(User user, V value, Permission permission) {
    for (PermissionEvaluator<K, V> evaluator : evaluators) {
      if (!evaluator.hasPermission(user, value, permission)) {
        return false;
      }
    }

    return true;
  }

}
