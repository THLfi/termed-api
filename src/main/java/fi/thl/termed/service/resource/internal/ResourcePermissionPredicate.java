package fi.thl.termed.service.resource.internal;

import java.util.function.Predicate;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;

public class ResourcePermissionPredicate implements Predicate<Resource> {

  private PermissionEvaluator<ClassId> classEvaluator;

  private User user;
  private Permission permission;

  public ResourcePermissionPredicate(PermissionEvaluator<ClassId> classEvaluator,
                                     User user, Permission permission) {
    this.classEvaluator = classEvaluator;
    this.user = user;
    this.permission = permission;
  }

  @Override
  public boolean test(Resource resource) {
    return classEvaluator.hasPermission(user, new ClassId(resource), permission);
  }

}
