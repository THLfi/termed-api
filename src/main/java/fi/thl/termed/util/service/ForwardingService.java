package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
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
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, SaveMode mode, WriteOptions opts,
      User user) {
    return delegate.deleteAndSave(deletes, saves, mode, opts, user);
  }

  @Override
  public List<V> getValues(User user) {
    return delegate.getValues(user);
  }

  @Override
  public List<V> getValues(Specification<K, V> spec, User user) {
    return delegate.getValues(spec, user);
  }

  @Override
  public List<V> getValues(Query<K, V> query, User user) {
    return delegate.getValues(query, user);
  }

  @Override
  public Stream<V> getValueStream(User user) {
    return delegate.getValueStream(user);
  }

  @Override
  public Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return delegate.getValueStream(spec, user);
  }

  @Override
  public Stream<V> getValueStream(Query<K, V> query, User user) {
    return delegate.getValueStream(query, user);
  }

  @Override
  public List<K> getKeys(User user) {
    return delegate.getKeys(user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> spec, User user) {
    return delegate.getKeys(spec, user);
  }

  @Override
  public List<K> getKeys(Query<K, V> query, User user) {
    return delegate.getKeys(query, user);
  }

  @Override
  public Stream<K> getKeyStream(User user) {
    return delegate.getKeyStream(user);
  }

  @Override
  public Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return delegate.getKeyStream(spec, user);
  }

  @Override
  public Stream<K> getKeyStream(Query<K, V> query, User user) {
    return delegate.getKeyStream(query, user);
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
