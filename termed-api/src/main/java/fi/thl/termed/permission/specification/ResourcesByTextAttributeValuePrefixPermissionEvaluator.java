package fi.thl.termed.permission.specification;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.resource.ResourcesByTextAttributeValuePrefix;

public class ResourcesByTextAttributeValuePrefixPermissionEvaluator
    implements PermissionEvaluator<ResourcesByTextAttributeValuePrefix> {

  private Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao;

  public ResourcesByTextAttributeValuePrefixPermissionEvaluator(
      Dao<ObjectRolePermission<TextAttributeId>, Void> textAttributePermissionDao) {
    this.textAttributePermissionDao = textAttributePermissionDao;
  }

  @Override
  public boolean hasPermission(User user,
                               ResourcesByTextAttributeValuePrefix specification,
                               Permission permission) {

    TextAttributeId textAttributeId = specification.getAttributeId();

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (textAttributePermissionDao.exists(
          new ObjectRolePermission<TextAttributeId>(textAttributeId, schemeRole, permission))) {
        return true;
      }
    }

    return false;
  }

}
