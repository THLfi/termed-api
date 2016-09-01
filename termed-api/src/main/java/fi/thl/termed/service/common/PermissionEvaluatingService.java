package fi.thl.termed.service.common;

import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.util.ValuePermissionEvaluatingPredicate;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.util.ListUtils;

/**
 * Simple implementation of object permission checking service. Save, delete and get (by id)
 * operations are pre authorized. Get operations returning lists of values are post filtered to
 * contain only permitted values.
 *
 * Save operations require both insert and update permissions as this implementation does not check
 * whether a previous value exists.
 */
public class PermissionEvaluatingService<K extends Serializable, V>
    extends ForwardingService<K, V> {

  protected PermissionEvaluator<K, V> evaluator;

  public PermissionEvaluatingService(Service<K, V> delegate, PermissionEvaluator<K, V> evaluator) {
    super(delegate);
    this.evaluator = evaluator;
  }

  @Override
  public void save(List<V> values, User currentUser) {
    for (V value : values) {
      if (!evaluator.hasPermission(currentUser, value, Permission.INSERT) ||
          !evaluator.hasPermission(currentUser, value, Permission.UPDATE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }

    super.save(values, currentUser);
  }

  @Override
  public void save(V value, User currentUser) {
    if (!evaluator.hasPermission(currentUser, value, Permission.INSERT) ||
        !evaluator.hasPermission(currentUser, value, Permission.UPDATE)) {
      throw new AccessDeniedException("Access is denied");
    }

    super.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    if (!evaluator.hasPermission(currentUser, id, Permission.DELETE)) {
      throw new AccessDeniedException("Access is denied");
    }

    super.delete(id, currentUser);
  }

  @Override
  public List<V> get(User currentUser) {
    return ListUtils.filter(
        super.get(currentUser),
        new ValuePermissionEvaluatingPredicate<V>(evaluator, currentUser, Permission.READ));
  }

  @Override
  public List<V> get(Specification<K, V> specification, User currentUser) {
    return ListUtils.filter(
        super.get(specification, currentUser),
        new ValuePermissionEvaluatingPredicate<V>(evaluator, currentUser, Permission.READ));
  }

  @Override
  public List<V> get(Query query, User currentUser) {
    return ListUtils.filter(
        super.get(query, currentUser),
        new ValuePermissionEvaluatingPredicate<V>(evaluator, currentUser, Permission.READ));
  }

  @Override
  public V get(K id, User currentUser) {
    if (!evaluator.hasPermission(currentUser, id, Permission.READ)) {
      throw new AccessDeniedException("Access is denied");
    }

    V value = super.get(id, currentUser);

    if (evaluator.hasPermission(currentUser, value, Permission.READ)) {
      return value;
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

}
