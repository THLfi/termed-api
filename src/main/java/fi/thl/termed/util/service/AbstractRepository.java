package fi.thl.termed.util.service;

import com.google.common.collect.MapDifference;
import fi.thl.termed.domain.Identifiable;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.SimpleValueDifference;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * For implementing a service that persists objects.
 */
public abstract class AbstractRepository<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  @Override
  public List<K> save(List<V> values, Map<String, Object> args, User user) {
    List<K> keys = new ArrayList<>();

    Map<K, V> inserts = new LinkedHashMap<>();
    Map<K, MapDifference.ValueDifference<V>> updates = new LinkedHashMap<>();

    for (V value : values) {
      K key = value.identifier();

      if (!exists(key, user)) {
        inserts.put(key, value);
      } else {
        updates.put(key, new SimpleValueDifference<>(value, get(key, user)
            .<IllegalStateException>orElseThrow(IllegalStateException::new)));
      }

      keys.add(key);
    }

    insert(inserts, user);
    update(updates, user);

    return keys;
  }

  @Override
  public K save(V value, Map<String, Object> args, User user) {
    K key = value.identifier();

    if (!exists(key, user)) {
      insert(key, value, user);
    } else {
      update(key, value, get(key, user).orElseThrow(IllegalStateException::new), user);
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
  public void delete(List<K> ids, Map<String, Object> args, User user) {
    ids.forEach(id -> delete(id, args, user));
  }

  @Override
  public void delete(K id, Map<String, Object> args, User user) {
    get(id, user).ifPresent(v -> delete(id, v, user));
  }

  @Override
  public Stream<V> get(List<K> ids, Map<String, Object> args, User user) {
    return ids.stream().map(id -> get(id, user)).filter(Optional::isPresent).map(Optional::get);
  }

  @Override
  public Optional<V> get(K id, Map<String, Object> args, User user) {
    return get(id, user);
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, Map<String, Object> args, User user) {
    return get(specification, user);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user) {
    return getKeys(specification, user);
  }

  public abstract boolean exists(K key, User user);

  public abstract void insert(K id, V value, User user);

  public abstract void update(K id, V newValue, V oldValue, User user);

  public abstract void delete(K id, V value, User user);

  public abstract Optional<V> get(K id, User user);

  public abstract Stream<V> get(Specification<K, V> specification, User currentUser);

  public abstract Stream<K> getKeys(Specification<K, V> specification, User currentUser);

}
