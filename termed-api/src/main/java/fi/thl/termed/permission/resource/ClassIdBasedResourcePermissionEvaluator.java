package fi.thl.termed.permission.resource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class ClassIdBasedResourcePermissionEvaluator implements PermissionEvaluator<ResourceId> {

  private PermissionEvaluator<ClassId> classIdPermissionEvaluator;

  public ClassIdBasedResourcePermissionEvaluator(
      PermissionEvaluator<ClassId> classIdPermissionEvaluator) {
    this.classIdPermissionEvaluator = classIdPermissionEvaluator;
  }

  @Override
  public boolean hasPermission(User user, ResourceId resourceId, Permission permission) {
    return classIdPermissionEvaluator.hasPermission(user, new ClassId(resourceId), permission);
  }

}
