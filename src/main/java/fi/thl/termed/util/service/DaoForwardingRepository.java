package fi.thl.termed.util.service;

import fi.thl.termed.domain.Identifiable;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Can be used to create simple service backed by a single DAO.
 */
public class DaoForwardingRepository<K extends Serializable, V extends Identifiable<K>> extends
    AbstractRepository<K, V> {

  private Dao<K, V> delegate;

  public DaoForwardingRepository(Dao<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean exists(K key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public void insert(K id, V value, User user) {
    delegate.insert(id, value, user);
  }

  @Override
  public void update(K id, V newValue, V oldValue, User user) {
    delegate.update(id, newValue, user);
  }

  @Override
  public void delete(K id, V value, User user) {
    delegate.delete(id, user);
  }

  @Override
  public List<V> get(Specification<K, V> specification, User user) {
    return delegate.getValues(specification, user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification, User user) {
    return delegate.getKeys(specification, user);
  }

  @Override
  public Optional<V> get(K id, User user) {
    return delegate.get(id, user);
  }

}
