package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ReferenceAttributePermissionsByReferenceAttributeId
    extends
    AbstractSqlSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> {

  private ReferenceAttributeId attributeId;

  public ReferenceAttributePermissionsByReferenceAttributeId(ReferenceAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean test(ObjectRolePermission<ReferenceAttributeId> objectRolePermission,
                      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), attributeId);
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

}
