package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;

public class RolePermissionsDtoToModel<K extends Serializable>
    implements
    Function<Multimap<String, Permission>, Map<ObjectRolePermission<K>, GrantedPermission>> {

  private SchemeId schemeId;
  private K objectId;

  public RolePermissionsDtoToModel(SchemeId schemeId, K objectId) {
    this.schemeId = schemeId;
    this.objectId = objectId;
  }

  public static <K extends Serializable> RolePermissionsDtoToModel<K> create(
      SchemeId schemeId, K objectId) {
    return new RolePermissionsDtoToModel<K>(schemeId, objectId);
  }

  @Override
  public Map<ObjectRolePermission<K>, GrantedPermission> apply(
      Multimap<String, Permission> rolePermissions) {
    Map<ObjectRolePermission<K>, GrantedPermission> model = Maps.newHashMap();

    for (Map.Entry<String, Permission> rolePermission : rolePermissions.entries()) {
      model.put(
          new ObjectRolePermission<K>(objectId, new SchemeRole(schemeId, rolePermission.getKey()),
                                      rolePermission.getValue()),
          null);
    }

    return model;
  }

}
