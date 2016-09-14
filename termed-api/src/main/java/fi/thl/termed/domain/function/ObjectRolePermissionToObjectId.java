package fi.thl.termed.domain.function;

import com.google.common.base.Function;

import java.io.Serializable;

import fi.thl.termed.domain.ObjectRolePermission;

public class ObjectRolePermissionToObjectId<K extends Serializable>
    implements Function<ObjectRolePermission<K>, K> {

  @Override
  public K apply(ObjectRolePermission<K> objectRolePermission) {
    return objectRolePermission.getObjectId();
  }

}
