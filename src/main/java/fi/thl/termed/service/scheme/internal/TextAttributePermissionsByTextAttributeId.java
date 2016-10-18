package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TextAttributePermissionsByTextAttributeId
    extends AbstractSqlSpecification<ObjectRolePermission<TextAttributeId>, GrantedPermission> {

  private TextAttributeId attributeId;

  public TextAttributePermissionsByTextAttributeId(TextAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean test(ObjectRolePermission<TextAttributeId> objectRolePermission,
                      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), attributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = attributeId.getDomainId();
    return new Object[]{domainId.getSchemeId(), domainId.getId(), attributeId.getId()};
  }

}
