package fi.thl.termed.util.query;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Models a specification that requires resolving (e.g. a sub specification of some sort)
 */
public interface DependentSpecification<K extends Serializable, V> extends Specification<K, V> {

  void resolve(Function<Specification<K, V>, Stream<K>> resolver);

}
