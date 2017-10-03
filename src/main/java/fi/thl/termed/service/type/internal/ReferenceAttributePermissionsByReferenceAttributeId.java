package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class ReferenceAttributePermissionsByReferenceAttributeId
    extends
    AbstractSqlSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> {

  private ReferenceAttributeId attributeId;

  ReferenceAttributePermissionsByReferenceAttributeId(ReferenceAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean test(ObjectRolePermission<ReferenceAttributeId> objectRolePermission,
      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), attributeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    TypeId domainId = attributeId.getDomainId();
    return ParametrizedSqlQuery.of(
        "reference_attribute_domain_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ?",
        domainId.getGraphId(), domainId.getId(), attributeId.getId());
  }

}
