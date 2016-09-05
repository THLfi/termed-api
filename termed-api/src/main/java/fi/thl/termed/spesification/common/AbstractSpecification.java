package fi.thl.termed.spesification.common;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.spesification.Specification;

public abstract class AbstractSpecification<K extends Serializable, V>
    implements Specification<K, V> {

  /**
   * To use with Guava Maps.filterEntries, delegates Predicate apply to accept.
   */
  @Override
  public boolean apply(Map.Entry<K, V> input) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(input.getKey());
    Preconditions.checkNotNull(input.getValue());
    return accept(input.getKey(), input.getValue());
  }

}
