package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.AbstractSpecification;

public class ReferenceAttributePermissionsByReferenceAttributeId
    extends AbstractSpecification<ObjectRolePermission<ReferenceAttributeId>, Void>
    implements SqlSpecification<ObjectRolePermission<ReferenceAttributeId>, Void> {

  private ReferenceAttributeId attributeId;

  public ReferenceAttributePermissionsByReferenceAttributeId(ReferenceAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean accept(ObjectRolePermission<ReferenceAttributeId> objectRolePermission,
                        Void value) {
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

}
