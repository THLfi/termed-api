package fi.thl.termed.dao.util;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.dao.AbstractSystemDao;
import fi.thl.termed.spesification.Specification;

/**
 * Simple memory based dao implementation. Useful e.g. in tests.
 */
public class MemoryBasedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

  private Map<K, V> data;

  public MemoryBasedSystemDao() {
    this(Maps.<K, V>newLinkedHashMap());
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
    return Maps.filterEntries(data, specification);
  }

  @Override
  public boolean exists(K key) {
    return data.containsKey(key);
  }

  @Override
  public V get(K key) {
    return data.get(key);
  }

}
