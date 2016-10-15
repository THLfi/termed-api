package fi.thl.termed.util.specification;

import com.google.common.base.Predicate;

import java.io.Serializable;
import java.util.Map;

public interface Specification<K extends Serializable, V> extends Predicate<Map.Entry<K, V>> {

}
