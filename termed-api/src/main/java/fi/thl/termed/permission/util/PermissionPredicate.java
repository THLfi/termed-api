package fi.thl.termed.permission.util;

import com.google.common.base.Predicate;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

/**
 * This predicate is useful for filtering collections of keys.
 */
public class PermissionPredicate<E> implements Predicate<E> {

  private PermissionEvaluator<E> evaluator;
  private User user;
  private Permission permission;

  public PermissionPredicate(PermissionEvaluator<E> evaluator, User user,
                             Permission permission) {
    this.evaluator = evaluator;
    this.user = user;
    this.permission = permission;
  }

  @Override
  public boolean apply(E object) {
    return evaluator.hasPermission(user, object, permission);
  }

}
