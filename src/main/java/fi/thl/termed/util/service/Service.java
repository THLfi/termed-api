package fi.thl.termed.util.service;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generic interface for services.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  default List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    List<K> ids = new ArrayList<>();
    values.forEach(value -> ids.add(save(value, mode, opts, user)));
    return ids;
  }

  /**
   * Save (insert or update) one value with dependencies.
   */
  K save(V value, SaveMode mode, WriteOptions opts, User user);

  /**
   * Delete values with dependencies by ids.
   */
  default void delete(List<K> ids, WriteOptions opts, User user) {
    ids.forEach(id -> delete(id, opts, user));
  }

  /**
   * Delete value with dependencies by id.
   */
  void delete(K id, WriteOptions opts, User user);

  /**
   * Save then delete values in one transaction. Returns ids of saved objects.
   */
  default List<K> saveAndDelete(List<V> saves, List<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    List<K> ids = save(saves, mode, opts, user);
    delete(deletes, opts, user);
    return ids;
  }

  /**
   * Get all values.
   */
  default List<V> getValues(User user) {
    try (Stream<V> values = getValueStream(user)) {
      return values.collect(toList());
    }
  }

  /**
   * Get specified values.
   */
  default List<V> getValues(Specification<K, V> spec, User user) {
    try (Stream<V> values = getValueStream(spec, user)) {
      return values.collect(toList());
    }
  }

  /**
   * Query values.
   */
  default List<V> getValues(Query<K, V> query, User user) {
    try (Stream<V> values = getValueStream(query, user)) {
      return values.collect(toList());
    }
  }

  /**
   * Get all values as stream. Stream is expected to be closed afterwards.
   */
  default Stream<V> getValueStream(User user) {
    return getValueStream(new Query<>(new MatchAll<>()), user);
  }

  /**
   * Get specified values as stream. Stream is expected to be closed afterwards.
   */
  default Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return getValueStream(new Query<>(spec), user);
  }

  /**
   * Query values as stream. Stream is expected to be closed afterwards.
   */
  Stream<V> getValueStream(Query<K, V> query, User user);

  /**
   * Get all keys.
   */
  default List<K> getKeys(User user) {
    try (Stream<K> keys = getKeyStream(user)) {
      return keys.collect(toList());
    }
  }

  /**
   * Get specified keys.
   */
  default List<K> getKeys(Specification<K, V> spec, User user) {
    try (Stream<K> keys = getKeyStream(spec, user)) {
      return keys.collect(toList());
    }
  }

  /**
   * Query keys.
   */
  default List<K> getKeys(Query<K, V> query, User user) {
    try (Stream<K> keys = getKeyStream(query, user)) {
      return keys.collect(toList());
    }
  }

  /**
   * Get all keys as stream. Stream is expected to be closed afterwards.
   */
  default Stream<K> getKeyStream(User user) {
    return getKeyStream(new Query<>(new MatchAll<>()), user);
  }

  /**
   * Get specified keys as stream. Stream is expected to be closed afterwards.
   */
  default Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return getKeyStream(new Query<>(spec), user);
  }

  /**
   * Query keys as stream. Stream is expected to be closed afterwards.
   */
  Stream<K> getKeyStream(Query<K, V> query, User user);

  /**
   * Count of specified values.
   */
  default long count(Specification<K, V> spec, User user) {
    try (Stream<K> keys = getKeyStream(spec, user)) {
      return keys.count();
    }
  }

  /**
   * Check if value with given id exists.
   */
  default boolean exists(K key, User user) {
    return get(key, user).isPresent();
  }

  /**
   * Get value by id.
   */
  Optional<V> get(K id, User user, Select... selects);

}
