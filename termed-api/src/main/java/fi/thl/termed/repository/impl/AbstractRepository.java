package fi.thl.termed.repository.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;

public abstract class AbstractRepository<K extends Serializable, V> implements Repository<K, V> {

  @Override
  public void save(List<V> values, User user) {
    for (V value : values) {
      save(value, user);
    }
  }

  // operations to help with crud operations and with updates delegated to other repositories

  protected void save(K key, V value, User user) {
    if (!exists(key, user)) {
      insert(key, value, user);
    } else {
      update(key, value, get(key, user).get(), user);
    }
  }

  protected abstract boolean exists(K key, User user);

  protected void insert(Map<K, V> map, User user) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      insert(entry.getKey(), entry.getValue(), user);
    }
  }

  protected abstract void insert(K id, V value, User user);

  protected void update(Map<K, MapDifference.ValueDifference<V>> differenceMap, User user) {
    for (Map.Entry<K, MapDifference.ValueDifference<V>> entry : differenceMap.entrySet()) {
      MapDifference.ValueDifference<V> difference = entry.getValue();
      update(entry.getKey(), difference.leftValue(), difference.rightValue(), user);
    }
  }

  protected abstract void update(K id, V newValue, V oldValue, User user);

  protected void delete(Map<K, V> map, User user) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      delete(entry.getKey(), entry.getValue(), user);
    }
  }

  protected abstract void delete(K id, V value, User user);

  @Override
  public List<V> get(List<K> ids, User user) {
    List<V> values = Lists.newArrayList();
    for (K id : ids) {
      values.add(get(id, user).get());
    }
    return values;
  }

}
