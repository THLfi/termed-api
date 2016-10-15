package fi.thl.termed.util.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.specification.TrueSpecification;

public abstract class AbstractDao<K extends Serializable, V> implements Dao<K, V> {

  @Override
  public void insert(Map<K, V> map, User user) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      insert(entry.getKey(), entry.getValue(), user);
    }
  }

  @Override
  public void update(Map<K, V> map, User user) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      update(entry.getKey(), entry.getValue(), user);
    }
  }

  @Override
  public void delete(List<K> keys, User user) {
    for (K key : keys) {
      delete(key, user);
    }
  }

  @Override
  public Map<K, V> getMap(User user) {
    return getMap(new TrueSpecification<K, V>(), user);
  }

  @Override
  public Map<K, V> getMap(List<K> keys, User user) {
    Map<K, V> map = Maps.newLinkedHashMap();
    for (K key : keys) {
      map.put(key, get(key, user).get());
    }
    return map;
  }

  @Override
  public List<K> getKeys(User user) {
    return getKeys(new TrueSpecification<K, V>(), user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification, User user) {
    return Lists.newArrayList(getMap(specification, user).keySet());
  }

  @Override
  public List<V> getValues(User user) {
    return getValues(new TrueSpecification<K, V>(), user);
  }

  @Override
  public List<V> getValues(Specification<K, V> specification, User user) {
    return Lists.newArrayList(getMap(specification, user).values());
  }

  @Override
  public List<V> getValues(List<K> keys, User user) {
    return Lists.newArrayList(getMap(keys, user).values());
  }

}
