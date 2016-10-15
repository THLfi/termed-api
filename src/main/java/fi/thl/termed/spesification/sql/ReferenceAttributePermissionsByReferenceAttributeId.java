package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class ReferenceAttributePermissionsByReferenceAttributeId
    extends AbstractSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission>
    implements SqlSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> {

  private ReferenceAttributeId attributeId;

  public ReferenceAttributePermissionsByReferenceAttributeId(ReferenceAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean accept(ObjectRolePermission<ReferenceAttributeId> objectRolePermission,
                        GrantedPermission value) {
    return Objects.equal(objectRolePermission.getObjectId(), attributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ?";
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
    ReferenceAttributePermissionsByReferenceAttributeId that =
        (ReferenceAttributePermissionsByReferenceAttributeId) o;
    return Objects.equal(attributeId, that.attributeId);
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
