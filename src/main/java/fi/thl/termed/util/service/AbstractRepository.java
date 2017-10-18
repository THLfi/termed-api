package fi.thl.termed.util.service;

import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRepository<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  private User helper = new User("abstract-repository-helper", "", SUPERUSER);

  @Override
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    List<K> keys = new ArrayList<>();

    Map<K, V> inserts = new LinkedHashMap<>();
    Map<K, V> updates = new LinkedHashMap<>();

    for (V value : values) {
      K key = value.identifier();

      if (mode == INSERT) {
        inserts.put(key, value);
      } else if (mode == UPDATE) {
        updates.put(key, value);
      } else if (exists(key, helper)) {
        updates.put(key, value);
      } else {
        inserts.put(key, value);
      }

      keys.add(key);
    }

    insert(inserts, mode, opts, user);
    update(updates, mode, opts, user);

    return keys;
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    K key = value.identifier();

    if (mode == INSERT) {
      insert(key, value, mode, opts, user);
    } else if (mode == UPDATE) {
      update(key, value, mode, opts, user);
    } else if (exists(key, helper)) {
      update(key, value, mode, opts, user);
    } else {
      insert(key, value, mode, opts, user);
    }

    return key;
  }

  protected void insert(Map<K, V> map, SaveMode mode, WriteOptions opts, User user) {
    map.forEach((k, v) -> insert(k, v, mode, opts, user));
  }

  protected void update(Map<K, V> map, SaveMode mode, WriteOptions opts, User user) {
    map.forEach((k, v) -> update(k, v, mode, opts, user));
  }

  protected abstract void insert(K id, V value, SaveMode mode, WriteOptions opts, User user);

  protected abstract void update(K id, V value, SaveMode mode, WriteOptions opts, User user);

}
