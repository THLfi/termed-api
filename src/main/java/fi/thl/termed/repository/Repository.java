package fi.thl.termed.repository;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.SpecificationQuery;

public interface Repository<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  List<K> save(List<V> values, User user);

  K save(V value, User user);

  /**
   * Delete value (with dependencies) by id.
   */
  void delete(K id, User user);

  /**
   * Query values. Values are expected to be fully populated.
   */
  List<V> get(SpecificationQuery<K, V> specification, User user);

  /**
   * Get values by ids. Value is expected to be fully populated.
   */
  List<V> get(List<K> ids, User user);

  /**
   * Get value by id. Value is expected to be fully populated.
   */
  Optional<V> get(K id, User user);

}
