package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;

public class RolePermissionsDtoToModel<K extends Serializable>
    implements
    Function<Multimap<String, Permission>, Map<ObjectRolePermission<K>, GrantedPermission>> {

  private GraphId graphId;
  private K objectId;

  public RolePermissionsDtoToModel(GraphId graphId, K objectId) {
    this.graphId = graphId;
    this.objectId = objectId;
  }

  @Override
  public Map<ObjectRolePermission<K>, GrantedPermission> apply(
      Multimap<String, Permission> rolePermissions) {
    Map<ObjectRolePermission<K>, GrantedPermission> model = Maps.newHashMap();

    for (Map.Entry<String, Permission> rolePermission : rolePermissions.entries()) {
      model.put(
          new ObjectRolePermission<>(objectId, new GraphRole(graphId, rolePermission.getKey()),
                                     rolePermission.getValue()),
          null);
    }

    return model;
  }

}
