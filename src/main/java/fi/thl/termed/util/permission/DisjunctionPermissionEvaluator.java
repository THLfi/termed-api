package fi.thl.termed.util.permission;

import com.google.common.collect.Lists;

import java.util.List;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;

/**
 * Accepts if any of the evaluators accepts.
 */
public class DisjunctionPermissionEvaluator<E> implements PermissionEvaluator<E> {

  private List<PermissionEvaluator<E>> evaluators;


  public DisjunctionPermissionEvaluator(PermissionEvaluator<E> e1,
                                        PermissionEvaluator<E> e2) {
    List<PermissionEvaluator<E>> list = Lists.newArrayList();
    list.add(e1);
    list.add(e2);
    this.evaluators = list;
  }

  public DisjunctionPermissionEvaluator(List<PermissionEvaluator<E>> evaluators) {
    this.evaluators = evaluators;
  }

  @Override
  public boolean hasPermission(User user, E object, Permission permission) {
    for (PermissionEvaluator<E> evaluator : evaluators) {
      if (evaluator.hasPermission(user, object, permission)) {
        return true;
      }
    }

    return false;
  }

}
