package fi.thl.termed.util.dao;

import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.permission.PermissionEvaluator;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizedDao<K extends Serializable, V> implements Dao<K, V> {

  private final SystemDao<K, V> delegate;

  private final PermissionEvaluator<K> evaluator;

  public AuthorizedDao(SystemDao<K, V> delegate, PermissionEvaluator<K> evaluator) {
    this.delegate = delegate;
    this.evaluator = evaluator;
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries, User user) {
    delegate.insert(entries.filter(e -> {
      if (evaluator.hasPermission(user, e._1, INSERT)) {
        return true;
      } else {
        throw new AccessDeniedException("Access is denied");
      }
    }));
  }

  @Override
  public void insert(K key, V val, User user) {
    if (evaluator.hasPermission(user, key, INSERT)) {
      delegate.insert(key, val);
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries, User user) {
    delegate.update(entries.filter(e -> {
      if (evaluator.hasPermission(user, e._1, UPDATE)) {
        return true;
      } else {
        throw new AccessDeniedException("Access is denied");
      }
    }));
  }

  @Override
  public void update(K key, V val, User user) {
    if (evaluator.hasPermission(user, key, UPDATE)) {
      delegate.update(key, val);
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

  @Override
  public void delete(Stream<K> keys, User user) {
    delegate.delete(keys.filter(k -> {
      if (evaluator.hasPermission(user, k, DELETE)) {
        return true;
      } else {
        throw new AccessDeniedException("Access is denied");
      }
    }));
  }

  @Override
  public void delete(K key, User user) {
    if (evaluator.hasPermission(user, key, DELETE)) {
      delegate.delete(key);
    } else {
      throw new AccessDeniedException("Access is denied");
    }
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification, User user) {
    return delegate.entries(specification)
        .filter(e -> evaluator.hasPermission(user, e._1, READ));
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification, User user) {
    return delegate.keys(specification)
        .filter(k -> evaluator.hasPermission(user, k, READ));
  }

  @Override
  public Stream<V> values(Specification<K, V> specification, User user) {
    return entries(specification, user).map(e -> e._2);
  }

  @Override
  public Optional<V> get(K key, User user) {
    return evaluator.hasPermission(user, key, READ) ? delegate.get(key) : Optional.empty();
  }

  @Override
  public boolean exists(K key, User user) {
    return evaluator.hasPermission(user, key, READ) && delegate.exists(key);
  }

}
