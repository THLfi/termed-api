package fi.thl.termed.util.service;

import static fi.thl.termed.util.collect.MapUtils.newLinkedHashMap;
import static java.util.Collections.emptyMap;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  @SuppressWarnings("unchecked")
  default List<K> save(List<V> values, User user, Entry<String, Object>... args) {
    return save(values, newLinkedHashMap(args), user);
  }

  List<K> save(List<V> values, Map<String, Object> args, User user);

  /**
   * Save (insert or update) value with dependencies.
   */
  default K save(V value, User user) {
    return save(value, emptyMap(), user);
  }

  @SuppressWarnings("unchecked")
  default K save(V value, User user, Entry<String, Object>... args) {
    return save(value, newLinkedHashMap(args), user);
  }

  K save(V value, Map<String, Object> args, User user);

  /**
   * Delete values (with dependencies) by ids.
   */
  default void delete(List<K> ids, User user) {
    delete(ids, emptyMap(), user);
  }

  @SuppressWarnings("unchecked")
  default void delete(List<K> ids, User user, Entry<String, Object>... args) {
    delete(ids, newLinkedHashMap(args), user);
  }

  void delete(List<K> ids, Map<String, Object> args, User user);

  /**
   * Delete value (with dependencies) by id.
   */
  default void delete(K id, User user) {
    delete(id, emptyMap(), user);
  }

  @SuppressWarnings("unchecked")
  default void delete(K id, User user, Entry<String, Object>... args) {
    delete(id, newLinkedHashMap(args), user);
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

  @SuppressWarnings("unchecked")
  default List<V> get(Specification<K, V> specification, User user, Entry<String, Object>... args) {
    return get(specification, newLinkedHashMap(args), user);
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

  @SuppressWarnings("unchecked")
  default List<V> getKeys(Specification<K, V> specification, User user,
      Entry<String, Object>... args) {
    return get(specification, newLinkedHashMap(args), user);
  }

  List<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user);

  /**
   * Get values by ids.
   */
  default List<V> get(List<K> ids, User user) {
    return get(ids, emptyMap(), user);
  }

  @SuppressWarnings("unchecked")
  default List<V> get(List<K> ids, User user, Entry<String, Object>... args) {
    return get(ids, newLinkedHashMap(args), user);
  }

  List<V> get(List<K> ids, Map<String, Object> args, User user);

  /**
   * Get value by id.
   */
  default Optional<V> get(K id, User user) {
    return get(id, emptyMap(), user);
  }

  @SuppressWarnings("unchecked")
  default Optional<V> get(K id, User user, Entry<String, Object>... args) {
    return get(id, newLinkedHashMap(args), user);
  }

  Optional<V> get(K id, Map<String, Object> args, User user);

}
