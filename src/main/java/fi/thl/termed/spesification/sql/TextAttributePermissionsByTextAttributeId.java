package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;

public class TextAttributePermissionsByTextAttributeId
    implements SqlSpecification<ObjectRolePermission<TextAttributeId>, GrantedPermission> {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextAttributePermissionsByTextAttributeId that = (TextAttributePermissionsByTextAttributeId) o;
    return Objects.equals(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(attributeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .toString();
  }

}
