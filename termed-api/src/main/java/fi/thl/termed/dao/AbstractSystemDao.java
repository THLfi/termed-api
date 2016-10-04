package fi.thl.termed.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.util.TrueSpecification;

public abstract class AbstractSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  @Override
  public void insert(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      insert(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void update(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      update(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void delete(List<K> keys) {
    for (K key : keys) {
      delete(key);
    }
  }

  @Override
  public Map<K, V> getMap() {
    return getMap(new TrueSpecification<K, V>());
  }

  @Override
  public Map<K, V> getMap(List<K> keys) {
    Map<K, V> map = Maps.newLinkedHashMap();
    for (K key : keys) {
      map.put(key, get(key));
    }
    return map;
  }

  @Override
  public List<K> getKeys() {
    return getKeys(new TrueSpecification<K, V>());
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    return Lists.newArrayList(getMap(specification).keySet());
  }

  @Override
  public List<V> getValues() {
    return getValues(new TrueSpecification<K, V>());
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    return Lists.newArrayList(getMap(specification).values());
  }

  @Override
  public List<V> getValues(List<K> keys) {
    List<V> values = Lists.newArrayList();
    for (K key : keys) {
      values.add(get(key));
    }
    return values;
  }

}
