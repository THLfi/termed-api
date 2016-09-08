package fi.thl.termed.permission.resource;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class DelegatingResourcePermissionEvaluator implements PermissionEvaluator<Resource> {

  private PermissionEvaluator<ResourceId> idEvaluator;

  public DelegatingResourcePermissionEvaluator(PermissionEvaluator<ResourceId> idEvaluator) {
    this.idEvaluator = idEvaluator;
  }

  @Override
  public boolean hasPermission(User user, Resource object, Permission permission) {
    return idEvaluator.hasPermission(user, new ResourceId(object), permission);
  }

}
