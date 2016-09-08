package fi.thl.termed.spesification;

import com.google.common.base.Predicate;

import java.io.Serializable;
import java.util.Map;

public interface Specification<K extends Serializable, V>
    extends Predicate<Map.Entry<K, V>>, Serializable {

  boolean accept(K key, V value);

}
