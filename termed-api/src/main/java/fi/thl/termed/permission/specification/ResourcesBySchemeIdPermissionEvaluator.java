package fi.thl.termed.permission.specification;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.lucene.ResourcesBySchemeId;

public class ResourcesBySchemeIdPermissionEvaluator
    implements PermissionEvaluator<ResourcesBySchemeId, Void> {

  private Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao;

  public ResourcesBySchemeIdPermissionEvaluator(
      Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao) {
    this.schemePermissionDao = schemePermissionDao;
  }

  public boolean hasPermission(User user, ResourcesBySchemeId specification,
                               Permission permission) {

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), specification.getSchemeId())) {
        if (schemePermissionDao.exists(
            new ObjectRolePermission<UUID>(specification.getSchemeId(),
                                           schemeRole.getRole(),
                                           permission))) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(User user, Void value, Permission permission) {
    throw new UnsupportedOperationException();
  }

}
