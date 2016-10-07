package fi.thl.termed.repository.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.util.SimpleValueDifference;

public abstract class AbstractRepository<K extends Serializable, V> implements Repository<K, V> {

  @Override
  public List<K> save(List<V> values, User user) {
    List<K> keys = Lists.newArrayList();

    Map<K, V> inserts = Maps.newLinkedHashMap();
    Map<K, MapDifference.ValueDifference<V>> updates = Maps.newLinkedHashMap();

    for (V value : values) {
      K key = extractKey(value);

      if (!exists(key, user)) {
        inserts.put(key, value);
      } else {
        updates.put(key, new SimpleValueDifference<V>(value, get(key, user).get()));
      }

      keys.add(key);
    }

    insert(inserts, user);
    update(updates, user);

    return keys;
  }

  @Override
  public K save(V value, User user) {
    K key = extractKey(value);

    if (!exists(key, user)) {
      insert(key, value, user);
    } else {
      update(key, value, get(key, user).get(), user);
    }

    return key;
  }

  /**
   * Find, create or extract key for given value.
   */
  protected abstract K extractKey(V value);

  // operations to help with crud operations and with updates delegated to other repositories

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
