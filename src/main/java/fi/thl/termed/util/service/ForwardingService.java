package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A service which simply forwards all requests to a delegate.
 */
public abstract class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    delegate.save(values, mode, opts, user);
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return delegate.save(value, mode, opts, user);
  }

  @Override
  public void delete(Stream<K> ids, WriteOptions opts, User user) {
    delegate.delete(ids, opts, user);
  }

  @Override
  public void delete(K key, WriteOptions opts, User user) {
    delegate.delete(key, opts, user);
  }

  @Override
  public void saveAndDelete(Stream<V> saves, Stream<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    delegate.saveAndDelete(saves, deletes, mode, opts, user);
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return delegate.values(query, user);
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return delegate.keys(query, user);
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    return delegate.count(spec, user);
  }

  @Override
  public boolean exists(K key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public Optional<V> get(K id, User user, Select... selects) {
    return delegate.get(id, user, selects);
  }

}
