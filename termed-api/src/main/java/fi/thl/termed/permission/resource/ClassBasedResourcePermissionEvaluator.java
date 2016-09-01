package fi.thl.termed.permission.resource;

import com.google.common.base.Objects;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class ClassBasedResourcePermissionEvaluator
    implements PermissionEvaluator<ResourceId, Resource> {

  private Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao;

  public ClassBasedResourcePermissionEvaluator(
      Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao) {
    this.classPermissionDao = classPermissionDao;
  }

  @Override
  public boolean hasPermission(User user, ResourceId resourceId, Permission permission) {
    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), resourceId.getSchemeId())) {
        if (classPermissionDao.exists(
            new ObjectRolePermission<ClassId>(new ClassId(resourceId),
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
