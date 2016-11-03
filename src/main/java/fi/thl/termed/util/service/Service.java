package fi.thl.termed.util.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;

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
  void delete(K id, User currentUser);

  /**
   * Get specified values.
   */
  Results<V> get(Query<K, V> query, User currentUser);

  /**
   * Get specified keys.
   */
  Results<K> getKeys(Query<K, V> query, User currentUser);

  /**
   * Get values by ids.
   */
  List<V> get(List<K> ids, User currentUser);

  /**
   * Get value by id.
   */
  Optional<V> get(K id, User currentUser);

}
