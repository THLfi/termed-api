package fi.thl.termed.exchange;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.SpecificationQuery;

/**
 * Typically a wrapper for service where values are converted to exported type before returning.
 */
public interface Exporter<K extends Serializable, V, E> {

  /**
   * Export specified values.
   */
  E get(SpecificationQuery<K, V> specification, Map<String, Object> args, User currentUser);

  /**
   * Export single value by id.
   */
  E get(K id, Map<String, Object> args, User currentUser);

}
