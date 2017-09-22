package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class TypePermissionsByTypeId
    extends AbstractSqlSpecification<ObjectRolePermission<TypeId>, GrantedPermission> {

  private TypeId typeId;

  public TypePermissionsByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(ObjectRolePermission<TypeId> objectRolePermission, GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), typeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "type_graph_id = ? and type_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
  }

}
