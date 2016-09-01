package fi.thl.termed.permission.resource;

import com.google.common.base.Objects;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.util.StrictLangValue;

public class ResourceTextAttributeValuePermissionEvaluator
    implements PermissionEvaluator<ResourceAttributeValueId, StrictLangValue> {

  private Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao;

  public ResourceTextAttributeValuePermissionEvaluator(
      Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao) {
    this.textAttributePermissionDao = textAttributePermissionDao;
  }

  @Override
  public boolean hasPermission(User user, ResourceAttributeValueId key, Permission permission) {
    ResourceId subjectId = key.getResourceId();

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), subjectId.getSchemeId())) {
        if (textAttributePermissionDao.exists(
            new ObjectRolePermission<TextAttributeId>(
                new TextAttributeId(new ClassId(subjectId), key.getAttributeId()),
                schemeRole.getRole(),
                permission))) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean hasPermission(User user, StrictLangValue value, Permission permission) {
    throw new UnsupportedOperationException();
  }

}
