package fi.thl.termed.spesification;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Map;

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

  protected abstract boolean accept(K key, V value);

}
