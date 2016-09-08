package fi.thl.termed.permission.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.io.Serializable;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.ConjunctionSpecification;
import fi.thl.termed.spesification.DisjunctionSpecification;
import fi.thl.termed.spesification.FalseSpecification;
import fi.thl.termed.spesification.Specification;

public class SpecificationPermissionFilter<K extends Serializable, V>
    implements Function<Specification<K, V>, Specification<K, V>> {

  private PermissionEvaluator<Specification<K, V>> evaluator;
  private User user;
  private Permission permission;

  public SpecificationPermissionFilter(PermissionEvaluator<Specification<K, V>> evaluator,
                                       User user, Permission permission) {
    this.evaluator = evaluator;
    this.user = user;
    this.permission = permission;
  }

  @Override
  public Specification<K, V> apply(Specification<K, V> spec) {
    return filter(spec);
  }

  private Specification<K, V> filter(Specification<K, V> spec) {
    if (spec instanceof DisjunctionSpecification) {
      return filter((DisjunctionSpecification<K, V>) spec);
    }
    if (spec instanceof ConjunctionSpecification) {
      return filter((ConjunctionSpecification<K, V>) spec);
    }
    return evaluator.hasPermission(user, spec, permission) ? spec : new FalseSpecification<K, V>();
  }

  private Specification<K, V> filter(DisjunctionSpecification<K, V> spec) {
    return new DisjunctionSpecification<K, V>(Lists.transform(spec.getSpecifications(), this));
  }

  private Specification<K, V> filter(ConjunctionSpecification<K, V> spec) {
    return new ConjunctionSpecification<K, V>(Lists.transform(spec.getSpecifications(), this));
  }

}
