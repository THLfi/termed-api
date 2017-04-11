package fi.thl.termed.util.specification;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Walks specification tree and checks each specification against given predicate. Replaces all
 * rejected specifications by MatchNone specification.
 */
public class SpecificationTreeFilter<K extends Serializable, V> implements
    Function<Specification<K, V>, Specification<K, V>> {

  private Predicate<Specification<K, V>> predicate;

  public SpecificationTreeFilter(Predicate<Specification<K, V>> predicate) {
    this.predicate = predicate;
  }

  @Override
  public Specification<K, V> apply(Specification<K, V> spec) {
    if (!predicate.test(spec)) {
      return new MatchNone<>();
    }

    if (spec instanceof AndSpecification) {
      return filterAndSpecification((AndSpecification<K, V>) spec);
    }
    if (spec instanceof OrSpecification) {
      return filterOrSpecification((OrSpecification<K, V>) spec);
    }

    return spec;
  }

  private AndSpecification<K, V> filterAndSpecification(AndSpecification<K, V> andSpec) {
    AndSpecification<K, V> filtered = new AndSpecification<>();
    andSpec.forEach(s -> filtered.addSpecification(apply(s)));
    return filtered;
  }

  private OrSpecification<K, V> filterOrSpecification(OrSpecification<K, V> orSpec) {
    OrSpecification<K, V> filtered = new OrSpecification<>();
    orSpec.forEach(s -> filtered.addSpecification(apply(s)));
    return filtered;
  }

}
