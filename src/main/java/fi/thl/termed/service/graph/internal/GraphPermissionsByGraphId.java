package fi.thl.termed.service.graph.internal;

import java.util.Objects;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class GraphPermissionsByGraphId
    extends AbstractSqlSpecification<ObjectRolePermission<GraphId>, GrantedPermission> {

  private GraphId graphId;

  public GraphPermissionsByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(ObjectRolePermission<GraphId> objectRolePermission,
                      GrantedPermission value) {
    return Objects.equals(objectRolePermission.getObjectId(), graphId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "graph_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{graphId.getId()};
  }

}
