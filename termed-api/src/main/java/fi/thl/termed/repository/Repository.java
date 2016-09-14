package fi.thl.termed.repository;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.SpecificationQuery;

public interface Repository<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  void save(List<V> values, User user);

  void save(V value, User user);

  /**
   * Delete value (with dependencies) by id.
   */
  void delete(K id, User user);

  /**
   * Query values. Values are expected to be fully populated.
   */
  List<V> get(SpecificationQuery<K, V> specification, User user);

  /**
   * Get value by id. Value is expected to be fully populated.
   */
  V get(K id, User user);

}
