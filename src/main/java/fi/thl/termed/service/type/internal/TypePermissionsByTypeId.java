package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TypePermissionsByTypeId
    extends AbstractSqlSpecification<ObjectRolePermission<TypeId>, GrantedPermission> {

  private TypeId typeId;

  TypePermissionsByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(ObjectRolePermission<TypeId> objectRolePermission, GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), typeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("type_graph_id = ? and type_id = ?",
        typeId.getGraphId(), typeId.getId());
  }

}
