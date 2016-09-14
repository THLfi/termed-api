package fi.thl.termed.permission.specification;

import java.util.UUID;

import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.resource.ResourcesBySchemeId;

public class ResourcesBySchemeIdPermissionEvaluator
    implements PermissionEvaluator<ResourcesBySchemeId> {

  private SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionDao;

  public ResourcesBySchemeIdPermissionEvaluator(
      SystemDao<ObjectRolePermission<UUID>, Void> schemePermissionDao) {
    this.schemePermissionDao = schemePermissionDao;
  }

  public boolean hasPermission(User user, ResourcesBySchemeId specification,
                               Permission permission) {

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (schemePermissionDao.exists(
          new ObjectRolePermission<UUID>(specification.getSchemeId(), schemeRole, permission))) {
        return true;
      }
    }

    return false;
  }

}
