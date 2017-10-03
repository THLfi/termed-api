package fi.thl.termed.service.graph.internal;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class GraphPermissionsByGraphId
    extends AbstractSqlSpecification<ObjectRolePermission<GraphId>, GrantedPermission> {

  private GraphId graphId;

  GraphPermissionsByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(ObjectRolePermission<GraphId> objectRolePermission,
      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), graphId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("graph_id = ?", graphId.getId());
  }

}
