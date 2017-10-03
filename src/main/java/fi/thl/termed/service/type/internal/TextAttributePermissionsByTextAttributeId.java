package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TextAttributePermissionsByTextAttributeId
    extends AbstractSqlSpecification<ObjectRolePermission<TextAttributeId>, GrantedPermission> {

  private TextAttributeId attributeId;

  TextAttributePermissionsByTextAttributeId(TextAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  @Override
  public boolean test(ObjectRolePermission<TextAttributeId> objectRolePermission,
      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), attributeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    TypeId domainId = attributeId.getDomainId();
    return ParametrizedSqlQuery.of(
        "text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ?",
        domainId.getGraphId(), domainId.getId(), attributeId.getId());
  }

}
