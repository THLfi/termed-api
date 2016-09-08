package fi.thl.termed.service.common;

import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.util.PermissionPredicate;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SpecificationQuery;
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

  protected PermissionEvaluator<K> keyEvaluator;
  protected PermissionEvaluator<V> valueEvaluator;
  protected PermissionEvaluator<Specification<K, V>> specificationEvaluator;

  public PermissionEvaluatingService(
      Service<K, V> delegate,
      PermissionEvaluator<K> keyEvaluator,
      PermissionEvaluator<V> valueEvaluator,
      PermissionEvaluator<Specification<K, V>> specificationEvaluator) {
    super(delegate);
    this.keyEvaluator = keyEvaluator;
    this.valueEvaluator = valueEvaluator;
    this.specificationEvaluator = specificationEvaluator;
  }

  @Override
  public void save(List<V> values, User currentUser) {
    for (V value : values) {
      if (!valueEvaluator.hasPermission(currentUser, value, Permission.INSERT) ||
          !valueEvaluator.hasPermission(currentUser, value, Permission.UPDATE)) {
        throw new AccessDeniedException("Access is denied");
      }
    }

    super.save(values, currentUser);
  }

  @Override
  public void save(V value, User currentUser) {
    if (!valueEvaluator.hasPermission(currentUser, value, Permission.INSERT) ||
        !valueEvaluator.hasPermission(currentUser, value, Permission.UPDATE)) {
      throw new AccessDeniedException("Access is denied");
    }

    super.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    if (!keyEvaluator.hasPermission(currentUser, id, Permission.DELETE)) {
      throw new AccessDeniedException("Access is denied");
    }

    super.delete(id, currentUser);
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> query, User currentUser) {
    if (specificationEvaluator
        .hasPermission(currentUser, query.getSpecification(), Permission.READ)) {
      return ListUtils.filter(
          super.get(query, currentUser),
          new PermissionPredicate<V>(valueEvaluator, currentUser, Permission.READ));
    }
    throw new AccessDeniedException("Access is denied");
  }

  @Override
  public V get(K id, User currentUser) {
    if (!keyEvaluator.hasPermission(currentUser, id, Permission.READ)) {
      throw new AccessDeniedException("Access is denied");
    }

    V value = super.get(id, currentUser);

    if (valueEvaluator.hasPermission(currentUser, value, Permission.READ)) {
      return value;
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

}
