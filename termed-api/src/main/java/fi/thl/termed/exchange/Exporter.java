package fi.thl.termed.exchange;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.Specification;

/**
 * Typically a wrapper for service where values are converted to exported type before returning.
 */
public interface Exporter<K extends Serializable, V, E> {

  /**
   * Export all values.
   */
  E get(Map<String, Object> args, User currentUser);

  /**
   * Export specified values.
   */
  E get(Specification<K, V> specification, Map<String, Object> args, User currentUser);

  /**
   * Export values conforming to given search query.
   */
  E get(Query query, Map<String, Object> args, User currentUser);

  /**
   * Export single value by id.
   */
  E get(K id, Map<String, Object> args, User currentUser);

}
