package fi.thl.termed.util.permission;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;

/**
 * Evaluator that is composed of multiple specification type specific evaluators.
 */
public class TypeBasedDelegatingSpecificationEvaluator<K extends Serializable, V>
    implements PermissionEvaluator<Specification<K, V>> {

  private Map<Class<? extends Specification<K, V>>,
      PermissionEvaluator<? extends Specification<K, V>>> evaluators = Maps.newHashMap();

  @SuppressWarnings("unchecked")
  public <S extends Specification<K, V>> void registerEvaluatorByRawType(
      Class type, PermissionEvaluator<S> evaluator) {
    registerEvaluator((Class<S>) type, evaluator);
  }

  public <S extends Specification<K, V>> void registerEvaluator(
      Class<S> type, PermissionEvaluator<S> evaluator) {
    evaluators.put(type, evaluator);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean hasPermission(User user, Specification<K, V> specification,
                               Permission permission) {

    Class type = specification.getClass();

    if (evaluators.containsKey(type)) {
      PermissionEvaluator evaluator = (PermissionEvaluator) evaluators.get(type);
      return evaluator.hasPermission(user, specification, permission);
    }

    throw new RuntimeException("No permission evaluator found for " + type.getSimpleName());
  }

}
