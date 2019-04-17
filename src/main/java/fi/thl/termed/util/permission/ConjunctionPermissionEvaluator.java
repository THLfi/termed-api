package fi.thl.termed.util.permission;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;

/**
 * Accepts if all evaluators accept. Rejects if any of the evaluators denies permission.
 */
public class ConjunctionPermissionEvaluator<E> implements PermissionEvaluator<E> {

  private final PermissionEvaluator<E>[] evaluators;

  public ConjunctionPermissionEvaluator(PermissionEvaluator<E>... evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    for (PermissionEvaluator<E> evaluator : evaluators) {
      if (!evaluator.hasPermission(user, object, permission)) {
        return false;
      }
    }

    return true;
  }

}
