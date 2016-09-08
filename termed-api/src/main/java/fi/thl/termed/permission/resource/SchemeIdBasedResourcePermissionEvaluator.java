package fi.thl.termed.permission.resource;

import java.util.UUID;

import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class SchemeIdBasedResourcePermissionEvaluator implements PermissionEvaluator<ResourceId> {

  private PermissionEvaluator<UUID> schemeIdPermissionEvaluator;

  public SchemeIdBasedResourcePermissionEvaluator(
      PermissionEvaluator<UUID> schemeIdPermissionEvaluator) {
    this.schemeIdPermissionEvaluator = schemeIdPermissionEvaluator;
  }

  @Override
  public boolean hasPermission(User user, ResourceId resourceId, Permission permission) {
    return schemeIdPermissionEvaluator.hasPermission(user, resourceId.getSchemeId(), permission);
  }

}
