package fi.thl.termed.util.service;

import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.spring.exception.BadRequestException;
import fi.thl.termed.util.spring.exception.NotFoundException;
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

      boolean exists = exists(key, helper);

      if (exists && (mode == UPDATE || mode == UPSERT)) {
        updates.put(key, value);
      } else if (!exists && (mode == INSERT || mode == UPSERT)) {
        inserts.put(key, value);
      } else if (exists == exists(key, user)) {
        throw new BadRequestException();
      } else {
        throw new NotFoundException();
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

    boolean exists = exists(key, helper);
    boolean existsForUser = exists(key, user);

    if (exists && (mode == UPDATE || mode == UPSERT)) {
      update(key, value, mode, opts, user);
    } else if (!exists && (mode == INSERT || mode == UPSERT)) {
      insert(key, value, mode, opts, user);
    } else if (exists == existsForUser) {
      throw new BadRequestException();
    } else {
      throw new NotFoundException();
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
