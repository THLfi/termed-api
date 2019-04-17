package fi.thl.termed.util.permission;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;

/**
 * Accepts if any of the evaluators accepts.
 */
public class DisjunctionPermissionEvaluator<E> implements PermissionEvaluator<E> {

  private final PermissionEvaluator<E>[] evaluators;

  public DisjunctionPermissionEvaluator(PermissionEvaluator<E>... evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    for (PermissionEvaluator<E> evaluator : evaluators) {
      if (evaluator.hasPermission(user, object, permission)) {
        return true;
      }
    }

    return false;
  }

}
