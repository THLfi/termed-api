package fi.thl.termed.util.service;

import fi.thl.termed.domain.Identifiable;
import fi.thl.termed.domain.User;
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
    Map<K, V> updates = new LinkedHashMap<>();

    for (V value : values) {
      K key = value.identifier();

      if (!exists(key, user)) {
        inserts.put(key, value);
      } else {
        updates.put(key, value);
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
      update(key, value, user);
    }

    return key;
  }

  protected abstract boolean exists(K key, User user);

  protected void insert(Map<K, V> map, User user) {
    map.forEach((k, v) -> insert(k, v, user));
  }

  protected void update(Map<K, V> map, User user) {
    map.forEach((k, v) -> update(k, v, user));
  }

  protected abstract void insert(K id, V value, User user);

  protected abstract void update(K id, V value, User user);

  @Override
  public void delete(List<K> ids, Map<String, Object> args, User user) {
    ids.forEach(id -> delete(id, args, user));
  }

  @Override
  public List<K> deleteAndSave(List<K> delete, List<V> save, Map<String, Object> args, User user) {
    delete(delete, args, user);
    return save(save, args, user);
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

}
