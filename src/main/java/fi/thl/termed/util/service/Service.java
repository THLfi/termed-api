package fi.thl.termed.util.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;
import fi.thl.termed.util.specification.Specification;

/**
 * Generic interface for services.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  List<K> save(List<V> values, User currentUser);

  K save(V value, User currentUser);

  /**
   * Delete value (with dependencies) by id.
   */
  void delete(List<K> ids, User currentUser);

  void delete(K id, User currentUser);

  /**
   * Get specified values.
   */
  Results<V> get(Query<K, V> query, User currentUser);

  default List<V> get(User currentUser) {
    return get(new MatchAll<>(), currentUser);
  }

  default List<V> get(Specification<K, V> specification, User currentUser) {
    return get(new Query<>(specification), currentUser).getValues();
  }

  default Optional<V> getFirst(Specification<K, V> specification, User currentUser) {
    return get(specification, currentUser).stream().findFirst();
  }

  /**
   * Get specified keys.
   */
  Results<K> getKeys(Query<K, V> query, User currentUser);

  default List<K> getKeys(User currentUser) {
    return getKeys(new MatchAll<>(), currentUser);
  }

  default List<K> getKeys(Specification<K, V> specification, User currentUser) {
    return getKeys(new Query<>(specification), currentUser).getValues();
  }

  default Optional<K> getFirstKey(Specification<K, V> specification, User currentUser) {
    return getKeys(specification, currentUser).stream().findFirst();
  }

  /**
   * Get values by ids.
   */
  List<V> get(List<K> ids, User currentUser);

  /**
   * Get value by id.
   */
  Optional<V> get(K id, User currentUser);

}
