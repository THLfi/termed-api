package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A service which simply forwards all requests to a delegate.
 */
public class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<K> save(List<V> values, User user, Arg... args) {
    return delegate.save(values, user, args);
  }

  @Override
  public K save(V value, User user, Arg... args) {
    return delegate.save(value, user, args);
  }

  @Override
  public void delete(List<K> ids, User user, Arg... args) {
    delegate.delete(ids, user, args);
  }

  @Override
  public void delete(K id, User user, Arg... args) {
    delegate.delete(id, user, args);
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> save, User user, Arg... args) {
    return delegate.deleteAndSave(deletes, save, user, args);
  }

  @Override
  public Stream<V> get(User user, Arg... args) {
    return delegate.get(user, args);
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, User user, Arg... args) {
    return delegate.get(specification, user, args);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, User user, Arg... args) {
    return delegate.getKeys(specification, user, args);
  }

  @Override
  public long count(Specification<K, V> specification, User user, Arg... args) {
    return delegate.count(specification, user, args);
  }

  @Override
  public boolean exists(K key, User user, Arg... args) {
    return delegate.exists(key, user, args);
  }

  @Override
  public Stream<V> get(List<K> ids, User user, Arg... args) {
    return delegate.get(ids, user, args);
  }

  @Override
  public Optional<V> get(K id, User user, Arg... args) {
    return delegate.get(id, user, args);
  }

}
