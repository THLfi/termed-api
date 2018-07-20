package fi.thl.termed.domain.transform;

import com.google.common.collect.Multimap;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

public class RolePermissionsDtoToModel<K extends Serializable> implements
    Function<Multimap<String, Permission>, Stream<Tuple2<ObjectRolePermission<K>, GrantedPermission>>> {

  private GraphId graphId;
  private K objectId;

  public RolePermissionsDtoToModel(GraphId graphId, K objectId) {
    this.graphId = graphId;
    this.objectId = objectId;
  }

  @Override
  public Stream<Tuple2<ObjectRolePermission<K>, GrantedPermission>> apply(
      Multimap<String, Permission> rolePermissions) {
    return rolePermissions.entries().stream().map(e -> Tuple.of(
        new ObjectRolePermission<>(objectId, new GraphRole(graphId, e.getKey()), e.getValue()),
        GrantedPermission.INSTANCE));
  }

}
