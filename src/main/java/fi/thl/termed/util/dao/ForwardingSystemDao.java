package fi.thl.termed.util.dao;

import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForwardingSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private SystemDao<K, V> delegate;

  public ForwardingSystemDao(SystemDao<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void insert(Map<K, V> map) {
    delegate.insert(map);
  }

  @Override
  public void insert(K key, V value) {
    delegate.insert(key, value);
  }

  @Override
  public void update(Map<K, V> map) {
    delegate.update(map);
  }

  @Override
  public void update(K key, V value) {
    delegate.update(key, value);
  }

  @Override
  public void delete(List<K> keys) {
    delegate.delete(keys);
  }

  @Override
  public void delete(K key) {
    delegate.delete(key);
  }

  @Override
  public Map<K, V> getMap() {
    return delegate.getMap();
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    return delegate.getMap(specification);
  }

  @Override
  public Map<K, V> getMap(List<K> keys) {
    return delegate.getMap(keys);
  }

  @Override
  public List<K> getKeys() {
    return delegate.getKeys();
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    return delegate.getKeys(specification);
  }

  @Override
  public List<V> getValues() {
    return delegate.getValues();
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    return delegate.getValues(specification);
  }

  @Override
  public List<V> getValues(List<K> keys) {
    return delegate.getValues(keys);
  }

  @Override
  public boolean exists(K key) {
    return delegate.exists(key);
  }

  @Override
  public Optional<V> get(K key) {
    return delegate.get(key);
  }

}
