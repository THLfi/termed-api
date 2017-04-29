package fi.thl.termed.util.service;

import static java.util.Collections.emptyMap;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
   * Get specified values.
   */
  default List<V> get(User user) {
    return get(new MatchAll<>(), user);
  }

  default List<V> get(Specification<K, V> specification, User user) {
    return get(specification, emptyMap(), user);
  }

  List<V> get(Specification<K, V> specification, Map<String, Object> args, User user);

  /**
   * Get specified keys.
   */
  default List<K> getKeys(User user) {
    return getKeys(new MatchAll<>(), user);
  }

  default List<K> getKeys(Specification<K, V> specification, User user) {
    return getKeys(specification, emptyMap(), user);
  }

  List<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user);

  /**
   * Get values by ids.
   */
  default List<V> get(List<K> ids, User user) {
    return get(ids, emptyMap(), user);
  }

  List<V> get(List<K> ids, Map<String, Object> args, User user);

  /**
   * Get value by id.
   */
  default Optional<V> get(K id, User user) {
    return get(id, emptyMap(), user);
  }

  Optional<V> get(K id, Map<String, Object> args, User user);

}
