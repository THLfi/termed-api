package fi.thl.termed.service;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.Specification;

/**
 * Generic interface for services.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  void save(List<V> values, User currentUser);

  void save(V value, User currentUser);

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
