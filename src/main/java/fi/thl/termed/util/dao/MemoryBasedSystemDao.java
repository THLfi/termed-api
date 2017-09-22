package fi.thl.termed.util.dao;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import fi.thl.termed.util.query.Specification;

/**
 * Simple memory based dao implementation. Useful e.g. in tests.
 */
public class MemoryBasedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

  private Map<K, V> data;

  public MemoryBasedSystemDao() {
    this(new LinkedHashMap<>());
  }

  public MemoryBasedSystemDao(Map<K, V> data) {
    this.data = data;
  }

  @Override
  public void insert(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void update(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void delete(K key) {
    data.remove(key);
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    return Maps.filterEntries(data, e -> specification.test(e.getKey(), e.getValue()));
  }

  @Override
  public boolean exists(K key) {
    return data.containsKey(key);
  }

  @Override
  public Optional<V> get(K key) {
    return Optional.ofNullable(data.get(key));
  }

}
