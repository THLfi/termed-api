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
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    return delegate.save(values, mode, opts, user);
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return delegate.save(value, mode, opts, user);
  }

  @Override
  public void delete(List<K> ids, WriteOptions opts, User user) {
    delegate.delete(ids, opts, user);
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    delegate.delete(id, opts, user);
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> save, SaveMode mode, WriteOptions opts,
      User user) {
    return delegate.deleteAndSave(deletes, save, mode, opts, user);
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
