package fi.thl.termed.permission.resource;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class SchemeBasedResourcePermissionEvaluator
    implements PermissionEvaluator<ResourceId, Resource> {

  private Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao;

  public SchemeBasedResourcePermissionEvaluator(
      Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao) {
    this.schemePermissionDao = schemePermissionDao;
  }

  @Override
  public boolean hasPermission(User user, ResourceId resourceId, Permission permission) {
    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), resourceId.getSchemeId())) {
        if (schemePermissionDao.exists(
            new ObjectRolePermission<UUID>(resourceId.getSchemeId(),
                                           schemeRole.getRole(),
                                           permission))) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(User user, Resource resource, Permission permission) {
    return hasPermission(user, new ResourceId(resource), permission);
  }

}
