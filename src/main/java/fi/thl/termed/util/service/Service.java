package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
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
  List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user);

  /**
   * Save (insert or update) one value with dependencies.
   */
  K save(V value, SaveMode mode, WriteOptions opts, User user);

  /**
   * Delete values with dependencies by ids.
   */
  void delete(List<K> ids, WriteOptions opts, User user);

  /**
   * Delete value with dependencies by id.
   */
  void delete(K id, WriteOptions opts, User user);

  /**
   * Delete and save values in one transaction. Returns ids of saved objects.
   */
  List<K> deleteAndSave(List<K> deletes, List<V> save, SaveMode mode, WriteOptions opts, User user);

  /**
   * Get specified values.
   */
  default Stream<V> get(User user, Arg... args) {
    return get(new MatchAll<>(), user, args);
  }

  Stream<V> get(Specification<K, V> specification, User user, Arg... args);

  /**
   * Get specified keys.
   */
  default Stream<K> getKeys(User user, Arg... args) {
    return getKeys(new MatchAll<>(), user, args);
  }

  Stream<K> getKeys(Specification<K, V> specification, User user, Arg... args);

  /**
   * Count of specified values.
   */
  long count(Specification<K, V> specification, User user, Arg... args);

  /**
   * Check if value with given id exists.
   */
  boolean exists(K key, User user, Arg... args);

  /**
   * Get values by ids. By default missing values are filtered out in the result stream.
   */
  Stream<V> get(List<K> ids, User user, Arg... args);

  /**
   * Get value by id.
   */
  Optional<V> get(K id, User user, Arg... args);

}
