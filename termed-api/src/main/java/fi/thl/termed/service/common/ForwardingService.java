package fi.thl.termed.service.common;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.SpecificationQuery;

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
  public List<V> get(SpecificationQuery<K, V> specification, User currentUser) {
    return delegate.get(specification, currentUser);
  }

  @Override
  public List<V> get(List<K> ids, User currentUser) {
    return delegate.get(ids, currentUser);
  }

  @Override
  public Optional<V> get(K id, User currentUser) {
    return delegate.get(id, currentUser);
  }

}
