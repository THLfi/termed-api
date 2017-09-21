package fi.thl.termed.util.service;

import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPDATE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.exception.BadRequestException;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
      boolean existsForUser = exists(key, user);

      if (exists && (mode == UPDATE || mode == UPSERT)) {
        updates.put(key, value);
      } else if (!exists && (mode == INSERT || mode == UPSERT)) {
        inserts.put(key, value);
      } else if (existsForUser) {
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
    } else if (existsForUser) {
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

  public void delete(List<K> ids, WriteOptions opts, User user) {
    ids.forEach(id -> delete(id, opts, user));
  }

  public List<K> deleteAndSave(List<K> deletes, List<V> saves, SaveMode mode, WriteOptions opts,
      User user) {
    delete(deletes, opts, user);
    return save(saves, mode, opts, user);
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
