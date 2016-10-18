package fi.thl.termed.util.specification;

import java.io.Serializable;
import java.util.function.BiPredicate;

public interface Specification<K extends Serializable, V> extends BiPredicate<K, V> {

}
