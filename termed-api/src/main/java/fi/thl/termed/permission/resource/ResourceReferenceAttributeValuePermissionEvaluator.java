package fi.thl.termed.permission.resource;

import com.google.common.base.Objects;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;

public class ResourceReferenceAttributeValuePermissionEvaluator
    implements PermissionEvaluator<ResourceAttributeValueId, ResourceId> {

  private Dao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionDao;

  public ResourceReferenceAttributeValuePermissionEvaluator(
      Dao<ObjectRolePermission<ReferenceAttributeId>, Void> referenceAttributePermissionDao) {
    this.referenceAttributePermissionDao = referenceAttributePermissionDao;
  }

  @Override
  public boolean hasPermission(User user, ResourceAttributeValueId key, Permission permission) {
    ResourceId subjectId = key.getResourceId();

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), subjectId.getSchemeId())) {
        if (referenceAttributePermissionDao.exists(
            new ObjectRolePermission<ReferenceAttributeId>(
                new ReferenceAttributeId(new ClassId(subjectId), key.getAttributeId()),
                schemeRole.getRole(),
                permission))) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(User user, ResourceId value, Permission permission) {
    throw new UnsupportedOperationException();
  }

}
