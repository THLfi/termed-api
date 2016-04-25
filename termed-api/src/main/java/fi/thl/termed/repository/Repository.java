package fi.thl.termed.repository;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.spesification.Specification;

public interface Repository<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  void save(Iterable<V> values);

  void save(V value);

  /**
   * Delete values (with dependencies) by id.
   */
  void delete(Iterable<K> id);

  void delete(K id);

  /**
   * Check if value exists for given id.
   */
  boolean exists(K id);

  /**
   * Get all values. Values may not have all dependencies fully populated.
   */
  List<V> get();

  /**
   * Get values by id. Values are expected to be fully populated.
   */
  List<V> get(Iterable<K> ids);

  /**
   * Query values. Values are expected to be fully populated.
   */
  List<V> get(Specification<K, V> specification);

  /**
   * Get value by id. Value is expected to be fully populated.
   */
  V get(K id);

}
