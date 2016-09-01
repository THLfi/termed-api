package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;

public class RolePermissionsDtoToModel<K extends Serializable>
    implements Function<Multimap<String, Permission>, Map<ObjectRolePermission<K>, Void>> {

  private K objectId;

  public RolePermissionsDtoToModel(K objectId) {
    this.objectId = objectId;
  }

  public static <K extends Serializable> RolePermissionsDtoToModel<K> create(K objectId) {
    return new RolePermissionsDtoToModel<K>(objectId);
  }

  @Override
  public Map<ObjectRolePermission<K>, Void> apply(Multimap<String, Permission> rolePermissions) {
    Map<ObjectRolePermission<K>, Void> model = Maps.newHashMap();

    for (Map.Entry<String, Permission> rolePermission : rolePermissions.entries()) {
      model.put(
          new ObjectRolePermission<K>(objectId, rolePermission.getKey(), rolePermission.getValue()),
          null);
    }

    return model;
  }

}
