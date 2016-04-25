package fi.thl.termed.repository.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.repository.Repository;

public abstract class AbstractRepository<K extends Serializable, V> implements Repository<K, V> {

  @Override
  public void save(Iterable<V> values) {
    for (V value : values) {
      save(value);
    }
  }

  @Override
  public List<V> get(Iterable<K> ids) {
    List<V> values = Lists.newArrayList();
    for (K id : ids) {
      values.add(get(id));
    }
    return values;
  }

  @Override
  public void delete(Iterable<K> ids) {
    for (K id : ids) {
      delete(id);
    }
  }

  // operations to help with crud operations and with updates delegated to other repositories

  protected void save(K key, V value) {
    if (!exists(key)) {
      insert(key, value);
    } else {
      update(key, value, get(key));
    }
  }

  protected void insert(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      insert(entry.getKey(), entry.getValue());
    }
  }

  protected abstract void insert(K id, V value);

  protected void update(Map<K, MapDifference.ValueDifference<V>> differenceMap) {
    for (Map.Entry<K, MapDifference.ValueDifference<V>> entry : differenceMap.entrySet()) {
      MapDifference.ValueDifference<V> difference = entry.getValue();
      update(entry.getKey(), difference.leftValue(), difference.rightValue());
    }
  }

  protected abstract void update(K id, V newValue, V oldValue);

  protected void delete(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      delete(entry.getKey(), entry.getValue());
    }
  }

  protected abstract void delete(K id, V value);

}
