package fi.thl.termed.util.service;

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
   * Delete and save values in one transaction. Returns ids of saved objects.
   */
  default List<K> deleteAndSave(List<K> deletes, List<V> saves, SaveMode mode, WriteOptions opts,
      User user) {
    delete(deletes, opts, user);
    return save(saves, mode, opts, user);
  }

  /**
   * Get all values.
   */
  default Stream<V> getValues(User user) {
    return getValues(new Query<>(new MatchAll<>()), user);
  }

  /**
   * Get specified values.
   */
  default Stream<V> getValues(Specification<K, V> spec, User user) {
    return getValues(new Query<>(spec), user);
  }

  /**
   * Query values.
   */
  Stream<V> getValues(Query<K, V> query, User user);

  /**
   * Get all keys.
   */
  default Stream<K> getKeys(User user) {
    return getKeys(new Query<>(new MatchAll<>()), user);
  }

  /**
   * Get specified keys.
   */
  default Stream<K> getKeys(Specification<K, V> spec, User user) {
    return getKeys(new Query<>(spec), user);
  }

  /**
   * Query keys.
   */
  Stream<K> getKeys(Query<K, V> query, User user);

  /**
   * Count of specified values.
   */
  default long count(Specification<K, V> spec, User user) {
    return getKeys(spec, user).count();
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
