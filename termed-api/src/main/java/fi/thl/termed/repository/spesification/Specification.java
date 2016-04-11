package fi.thl.termed.repository.spesification;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import java.io.Serializable;
import java.util.Map;

public abstract class Specification<K extends Serializable, V>
    implements Predicate<Map.Entry<K, V>> {

  /**
   * Test whether to include entry into result set.
   */
  public abstract boolean accept(K key, V value);

  /**
   * To use with Guava Maps.filterEntries, delegates Predicate apply to include.
   */
  @Override
  public boolean apply(Map.Entry<K, V> input) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(input.getKey());
    Preconditions.checkNotNull(input.getValue());
    return accept(input.getKey(), input.getValue());
  }

}
