package fi.thl.termed.util.service;

import com.google.common.collect.MapDifference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.thl.termed.domain.Identifiable;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.SimpleValueDifference;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;
import fi.thl.termed.util.specification.Specification;

/**
 * For implementing a service that persists objects.
 */
public abstract class AbstractRepository<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  @Override
  public List<K> save(List<V> values, User user) {
    List<K> keys = new ArrayList<>();

    Map<K, V> inserts = new LinkedHashMap<>();
    Map<K, MapDifference.ValueDifference<V>> updates = new LinkedHashMap<>();

    for (V value : values) {
      K key = value.identifier();

      if (!exists(key, user)) {
        inserts.put(key, value);
      } else {
        updates.put(key, new SimpleValueDifference<>(value, get(key, user).get()));
      }

      keys.add(key);
    }

    insert(inserts, user);
    update(updates, user);

    return keys;
  }

  @Override
  public K save(V value, User user) {
    K key = value.identifier();

    if (!exists(key, user)) {
      insert(key, value, user);
    } else {
      update(key, value, get(key, user).get(), user);
    }

    return key;
  }

  public void insert(Map<K, V> map, User user) {
    map.forEach((k, v) -> insert(k, v, user));
  }

  public void update(Map<K, MapDifference.ValueDifference<V>> differenceMap, User user) {
    differenceMap.forEach((k, v) -> update(k, v.leftValue(), v.rightValue(), user));
  }

  public void delete(Map<K, V> map, User user) {
    map.forEach((k, v) -> delete(k, v, user));
  }

  @Override
  public void delete(K id, User user) {
    get(id, user).ifPresent(v -> delete(id, v, user));
  }

  @Override
  public List<V> get(List<K> ids, User user) {
    List<V> values = new ArrayList<>();
    for (K id : ids) {
      get(id, user).ifPresent(values::add);
    }
    return values;
  }

  @Override
  public Results<V> get(Query<K, V> specification, User user) {
    List<V> results = get(specification.getSpecification(), user);
    return new Results<>(results.subList(0, Math.min(results.size(), specification.getMax())),
                         results.size());
  }

  @Override
  public Results<K> getKeys(Query<K, V> specification, User user) {
    List<K> results = getKeys(specification.getSpecification(), user);
    return new Results<>(results.subList(0, Math.min(results.size(), specification.getMax())),
                         results.size());
  }

  protected abstract boolean exists(K key, User user);

  public abstract void insert(K id, V value, User user);

  public abstract void update(K id, V newValue, V oldValue, User user);

  public abstract void delete(K id, V value, User user);

  public abstract List<V> get(Specification<K, V> specification, User currentUser);

  public abstract List<K> getKeys(Specification<K, V> specification, User currentUser);

  public abstract Optional<V> get(K id, User user);

}
