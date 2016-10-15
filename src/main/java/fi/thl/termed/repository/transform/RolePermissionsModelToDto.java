package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;

public class RolePermissionsModelToDto<K extends Serializable>
    implements Function<Map<ObjectRolePermission<K>, GrantedPermission>, Multimap<String, Permission>> {

  public static <K extends Serializable> RolePermissionsModelToDto<K> create() {
    return new RolePermissionsModelToDto<K>();
  }

  @Override
  public Multimap<String, Permission> apply(Map<ObjectRolePermission<K>, GrantedPermission> input) {
    Multimap<String, Permission> map = LinkedHashMultimap.create();

    for (ObjectRolePermission<K> objectRolePermission : input.keySet()) {
      map.put(objectRolePermission.getRole(), objectRolePermission.getPermission());
    }

    return map;
  }

}
