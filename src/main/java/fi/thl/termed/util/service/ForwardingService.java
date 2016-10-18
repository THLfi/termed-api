package fi.thl.termed.util.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Query;

/**
 * A service which simply forwards all requests to a delegate (decorator pattern).
 */
public class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<K> save(List<V> values, User user) {
    return delegate.save(values, user);
  }

  @Override
  public K save(V value, User user) {
    return delegate.save(value, user);
  }

  @Override
  public void delete(K id, User user) {
    delegate.delete(id, user);
  }

  @Override
  public List<V> get(Query<K, V> specification, User user) {
    return delegate.get(specification, user);
  }

  @Override
  public List<K> getKeys(Query<K, V> specification, User user) {
    return delegate.getKeys(specification, user);
  }

  @Override
  public List<V> get(List<K> ids, User user) {
    return delegate.get(ids, user);
  }

  @Override
  public Optional<V> get(K id, User user) {
    return delegate.get(id, user);
  }

}
