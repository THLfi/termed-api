package fi.thl.termed.util.permission;

import java.util.function.Function;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;

/**
 * Evaluates an object by first mapping to target type T and then delegating to evaluator for type
 * T. Useful e.g. for re-using existing evaluators to different types. Useful also for evaluating a
 * bigger object based on some part of an object.
 */
public class MappingPermissionEvaluator<F, T> implements PermissionEvaluator<F> {

  private Function<F, T> mapping;
  private PermissionEvaluator<T> delegate;

  public MappingPermissionEvaluator(java.util.function.Function<F, T> mapping, PermissionEvaluator<T> delegate) {
    this.mapping = mapping;
    this.delegate = delegate;
  }

  @Override
  public boolean hasPermission(User user, F object, Permission permission) {
    return delegate.hasPermission(user, mapping.apply(object), permission);
  }

}
