package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.TextAttributeId;

public class TextAttributePermissionsByTextAttributeId
    extends SqlSpecification<ObjectRolePermission<TextAttributeId>, Void> {

  private TextAttributeId attributeId;

  public TextAttributePermissionsByTextAttributeId(TextAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean accept(ObjectRolePermission<TextAttributeId> objectRolePermission, Void value) {
    return Objects.equal(objectRolePermission.getObjectId(), attributeId);
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
