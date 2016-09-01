package fi.thl.termed.service.common;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

/**
 * A service which simply forwards all requests to a delegate (decorator pattern).
 */
public class ForwardingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;

  public ForwardingService(Service<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void save(List<V> values, User currentUser) {
    delegate.save(values, currentUser);
  }

  @Override
  public void save(V value, User currentUser) {
    delegate.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    delegate.delete(id, currentUser);
  }

  @Override
  public List<V> get(User currentUser) {
    return delegate.get(currentUser);
  }

  @Override
  public List<V> get(Specification<K, V> specification, User currentUser) {
    return delegate.get(specification, currentUser);
  }

  @Override
  public List<V> get(Query query, User currentUser) {
    return delegate.get(query, currentUser);
  }

  @Override
  public V get(K id, User currentUser) {
    return delegate.get(id, currentUser);
  }

}
