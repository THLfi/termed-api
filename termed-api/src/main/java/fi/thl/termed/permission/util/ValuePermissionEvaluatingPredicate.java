package fi.thl.termed.permission.util;

import com.google.common.base.Predicate;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * This predicate is useful for filtering collections of values.
 */
public class ValuePermissionEvaluatingPredicate<V> implements Predicate<V> {

  private PermissionEvaluator<?, V> evaluator;
  private User user;
  private Permission permission;

  public ValuePermissionEvaluatingPredicate(PermissionEvaluator<?, V> evaluator,
                                            User user,
                                            Permission permission) {
    this.evaluator = evaluator;
    this.user = user;
    this.permission = permission;
  }

  @Override
  public boolean apply(V value) {
    return evaluator.hasPermission(user, value, permission);
  }

}
