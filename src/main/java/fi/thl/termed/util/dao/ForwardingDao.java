package fi.thl.termed.util.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;

public class ForwardingDao<K extends Serializable, V> implements Dao<K, V> {

  private Dao<K, V> delegate;

  public ForwardingDao(Dao<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void insert(Map<K, V> map, User user) {
    delegate.insert(map, user);
  }

  @Override
  public void insert(K key, V value, User user) {
    delegate.insert(key, value, user);
  }

  @Override
  public void update(Map<K, V> map, User user) {
    delegate.update(map, user);
  }

  @Override
  public void update(K key, V value, User user) {
    delegate.update(key, value, user);
  }

  @Override
  public void delete(List<K> keys, User user) {
    delegate.delete(keys, user);
  }

  @Override
  public void delete(K key, User user) {
    delegate.delete(key, user);
  }

  @Override
  public Map<K, V> getMap(User user) {
    return delegate.getMap(user);
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification, User user) {
    return delegate.getMap(specification, user);
  }

  @Override
  public Map<K, V> getMap(List<K> keys, User user) {
    return delegate.getMap(keys, user);
  }

  @Override
  public List<K> getKeys(User user) {
    return delegate.getKeys(user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification, User user) {
    return delegate.getKeys(specification, user);
  }

  @Override
  public List<V> getValues(User user) {
    return delegate.getValues(user);
  }

  @Override
  public List<V> getValues(Specification<K, V> specification, User user) {
    return delegate.getValues(specification, user);
  }

  @Override
  public List<V> getValues(List<K> keys, User user) {
    return delegate.getValues(keys, user);
  }

  @Override
  public boolean exists(K key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public Optional<V> get(K key, User user) {
    return delegate.get(key, user);
  }

}
