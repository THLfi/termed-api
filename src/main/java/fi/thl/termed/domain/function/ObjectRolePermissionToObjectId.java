package fi.thl.termed.domain.function;

import java.io.Serializable;

import fi.thl.termed.domain.ObjectRolePermission;

public class ObjectRolePermissionToObjectId<K extends Serializable>
    implements java.util.function.Function<ObjectRolePermission<K>, K> {

  @Override
  public K apply(ObjectRolePermission<K> objectRolePermission) {
    return objectRolePermission.getObjectId();
  }

}
