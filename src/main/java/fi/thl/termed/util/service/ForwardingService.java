package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A service which simply forwards all requests to a delegate (decorator pattern).
 */
public class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<K> save(List<V> values, Map<String, Object> args, User user) {
    return delegate.save(values, args, user);
  }

  @Override
  public K save(V value, Map<String, Object> args, User user) {
    return delegate.save(value, args, user);
  }

  @Override
  public void delete(List<K> ids, Map<String, Object> args, User user) {
    delegate.delete(ids, args, user);
  }

  @Override
  public void delete(K id, Map<String, Object> args, User user) {
    delegate.delete(id, args, user);
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, Map<String, Object> args,
      User user) {
    return delegate.deleteAndSave(deletes, saves, args, user);
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, Map<String, Object> args, User user) {
    return delegate.get(specification, args, user);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user) {
    return delegate.getKeys(specification, args, user);
  }

  @Override
  public long count(User user) {
    return delegate.count(user);
  }

  @Override
  public long count(Specification<K, V> specification, User user) {
    return delegate.count(specification, user);
  }

  @Override
  public long count(Specification<K, V> specification, Map<String, Object> args, User user) {
    return delegate.count(specification, args, user);
  }

  @Override
  public Stream<V> get(List<K> ids, Map<String, Object> args, User user) {
    return delegate.get(ids, args, user);
  }

  @Override
  public Optional<V> get(K id, Map<String, Object> args, User user) {
    return delegate.get(id, args, user);
  }

}
