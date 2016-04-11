package fi.thl.termed.service;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.spesification.Specification;

/**
 * Generic interface for services. Probably no need to use this as a base if only few methods are
 * needed.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  int save(List<V> values, User currentUser);

  V save(K key, V value, User currentUser);

  V save(V value, User currentUser);

  /**
   * Delete value (with dependencies) by id.
   */
  void delete(K id, User currentUser);

  /**
   * Get all values. Values may not have all dependencies fully populated.
   */
  List<V> get(User currentUser);

  /**
   * Get specified values. Values are expected to be fully populated.
   */
  List<V> get(Specification<K, V> specification, User currentUser);

  /**
   * Search values. Values may not have all dependencies fully populated.
   */
  List<V> get(Query query, User currentUser);

  /**
   * Get value by id. Value is expected to be fully populated.
   */
  V get(K id, User currentUser);

}
