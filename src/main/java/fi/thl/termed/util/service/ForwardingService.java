package fi.thl.termed.util.service;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.SpecificationQuery;

/**
 * A service which simply forwards all requests to a delegate (decorator pattern).
 */
public class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<K> save(List<V> values, User currentUser) {
    return delegate.save(values, currentUser);
  }

  @Override
  public K save(V value, User currentUser) {
    return delegate.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    delegate.delete(id, currentUser);
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User currentUser) {
    return delegate.get(specification, currentUser);
  }

  @Override
  public List<V> get(List<K> ids, User currentUser) {
    return delegate.get(ids, currentUser);
  }

  @Override
  public java.util.Optional<V> get(K id, User currentUser) {
    return delegate.get(id, currentUser);
  }

}
