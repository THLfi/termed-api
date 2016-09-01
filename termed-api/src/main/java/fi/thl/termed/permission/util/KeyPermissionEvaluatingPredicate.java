package fi.thl.termed.permission.util;

import com.google.common.base.Predicate;

import java.io.Serializable;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * This predicate is useful for filtering collections of keys.
 */
public class KeyPermissionEvaluatingPredicate<K extends Serializable> implements Predicate<K> {

  private PermissionEvaluator<K, ?> evaluator;
  private User user;
  private Permission permission;

  public KeyPermissionEvaluatingPredicate(PermissionEvaluator<K, ?> evaluator,
                                          User user,
                                          Permission permission) {
    this.evaluator = evaluator;
    this.user = user;
    this.permission = permission;
  }

  @Override
  public boolean apply(K key) {
    return evaluator.hasPermission(user, key, permission);
  }

}
