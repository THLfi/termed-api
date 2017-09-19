package fi.thl.termed.util.service;

import static java.util.Collections.emptyMap;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generic interface for services.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  default List<K> save(List<V> values, User user) {
    return save(values, emptyMap(), user);
  }

  List<K> save(List<V> values, Map<String, Object> args, User user);

  /**
   * Save (insert or update) value with dependencies.
   */
  default K save(V value, User user) {
    return save(value, emptyMap(), user);
  }

  K save(V value, Map<String, Object> args, User user);

  /**
   * Delete values (with dependencies) by ids.
   */
  default void delete(List<K> ids, User user) {
    delete(ids, emptyMap(), user);
  }

  void delete(List<K> ids, Map<String, Object> args, User user);

  /**
   * Delete value (with dependencies) by id.
   */
  default void delete(K id, User user) {
    delete(id, emptyMap(), user);
  }

  void delete(K id, Map<String, Object> args, User user);

  /**
   * Delete and save values in one transaction. Returns ids of saved objects.
   */
  default List<K> deleteAndSave(List<K> deletes, List<V> saves, User user) {
    return deleteAndSave(deletes, saves, emptyMap(), user);
  }

  List<K> deleteAndSave(List<K> deletes, List<V> saves, Map<String, Object> args, User user);

  /**
   * Get specified values.
   */
  default Stream<V> get(User user) {
    return get(new MatchAll<>(), user);
  }

  default Stream<V> get(Specification<K, V> specification, User user) {
    return get(specification, emptyMap(), user);
  }

  Stream<V> get(Specification<K, V> specification, Map<String, Object> args, User user);

  /**
   * Get specified keys.
   */
  default Stream<K> getKeys(User user) {
    return getKeys(new MatchAll<>(), user);
  }

  default Stream<K> getKeys(Specification<K, V> specification, User user) {
    return getKeys(specification, emptyMap(), user);
  }

  Stream<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user);

  /**
   * Count of specified values.
   */
  default long count(User user) {
    return count(new MatchAll<>(), user);
  }

  default long count(Specification<K, V> specification, User user) {
    return count(specification, emptyMap(), user);
  }

  default long count(Specification<K, V> specification, Map<String, Object> args, User user) {
    return getKeys(specification, args, user).count();
  }

  /**
   * Get values by ids.
   */
  default Stream<V> get(List<K> ids, User user) {
    return get(ids, emptyMap(), user);
  }

  Stream<V> get(List<K> ids, Map<String, Object> args, User user);

  /**
   * Get value by id.
   */
  default Optional<V> get(K id, User user) {
    return get(id, emptyMap(), user);
  }

  Optional<V> get(K id, Map<String, Object> args, User user);

}
