package fi.thl.termed.permission.specification;

import com.google.common.base.Objects;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.spesification.lucene.ResourcesByTextAttributeValuePrefix;

public class ResourcesByTextAttributeValuePrefixPermissionEvaluator
    implements PermissionEvaluator<ResourcesByTextAttributeValuePrefix, Void> {

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
    ClassId domainId = textAttributeId.getDomainId();

    for (SchemeRole schemeRole : user.getSchemeRoles()) {
      if (Objects.equal(schemeRole.getSchemeId(), domainId.getSchemeId())) {
        if (textAttributePermissionDao.exists(
            new ObjectRolePermission<TextAttributeId>(textAttributeId,
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
