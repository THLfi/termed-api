package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractRepository<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  @Override
  public List<K> save(List<V> values, User user, Arg... args) {
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
  public K save(V value, User user, Arg... args) {
    K key = value.identifier();

    if (!exists(key, user)) {
      insert(key, value, user);
    } else {
      update(key, value, user);
    }

    return key;
  }

  protected void insert(Map<K, V> map, User user) {
    map.forEach((k, v) -> insert(k, v, user));
  }

  protected void update(Map<K, V> map, User user) {
    map.forEach((k, v) -> update(k, v, user));
  }

  protected abstract void insert(K id, V value, User user);

  protected abstract void update(K id, V value, User user);

  public void delete(List<K> ids, User user, Arg... args) {
    ids.forEach(id -> delete(id, user, args));
  }

  public List<K> deleteAndSave(List<K> deletes, List<V> saves, User user, Arg... args) {
    delete(deletes, user, args);
    return save(saves, user, args);
  }

  public long count(Specification<K, V> specification, User user, Arg... args) {
    return getKeys(specification, user, args).count();
  }

  public boolean exists(K key, User user, Arg... args) {
    return get(key, user, args).isPresent();
  }

  public Stream<V> get(List<K> ids, User u, Arg... args) {
    return ids.stream().map(id -> get(id, u, args)).filter(Optional::isPresent).map(Optional::get);
  }

}
