package fi.thl.termed.dao;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.spesification.Specification;

/**
 * Simple memory based dao implementation. Useful e.g. in tests.
 */
public class MemoryBasedDao<K extends Serializable, V> implements Dao<K, V> {

  private Map<K, V> data;

  public MemoryBasedDao() {
    this(Maps.<K, V>newLinkedHashMap());
  }

  public MemoryBasedDao(Map<K, V> data) {
    this.data = data;
  }

  @Override
  public void insert(Map<K, V> map) {
    data.putAll(map);
  }

  @Override
  public void insert(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void update(Map<K, V> map) {
    data.putAll(map);
  }

  @Override
  public void update(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void delete() {
    data.clear();
  }

  @Override
  public void delete(Iterable<K> keys) {
    data.keySet().removeAll(Lists.newArrayList(keys));
  }

  @Override
  public void delete(K key) {
    data.remove(key);
  }

  @Override
  public Map<K, V> getMap() {
    return data;
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    return Maps.filterEntries(data, specification);
  }

  @Override
  public Map<K, V> getMap(Iterable<K> keys) {
    return Maps.filterKeys(data, Predicates.in(Sets.newHashSet(keys)));
  }

  @Override
  public List<K> getKeys() {
    return Lists.newArrayList(data.keySet());
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    return Lists.newArrayList(getMap(specification).keySet());
  }

  @Override
  public List<V> getValues() {
    return Lists.newArrayList(data.values());
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    return Lists.newArrayList(getMap(specification).values());
  }

  @Override
  public List<V> getValues(Iterable<K> keys) {
    return Lists.newArrayList(getMap(keys).values());
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
