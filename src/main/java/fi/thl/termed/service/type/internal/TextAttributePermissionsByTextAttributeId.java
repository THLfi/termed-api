package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
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
    return "text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    TypeId domainId = attributeId.getDomainId();
    return new Object[]{domainId.getGraphId(), domainId.getId(), attributeId.getId()};
  }

}
