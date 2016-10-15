package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ObjectRolePermission<K extends Serializable> implements Serializable {

  private final K objectId;
  private final SchemeRole schemeRole;
  private final Permission permission;

  public ObjectRolePermission(K objectId, SchemeRole schemeRole, Permission permission) {
    this.objectId = checkNotNull(objectId, "objectId can't be null in %s", getClass());
    this.schemeRole = checkNotNull(schemeRole, "schemeRole can't be null in %s", getClass());
    this.permission = checkNotNull(permission, "permission can't be null in %s", getClass());
  }

  public K getObjectId() {
    return objectId;
  }

  public SchemeRole getSchemeRole() {
    return schemeRole;
  }

  public String getRole() {
    return schemeRole != null ? schemeRole.getRole() : null;
  }

  public Permission getPermission() {
    return permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectRolePermission<?> that = (ObjectRolePermission<?>) o;
    return Objects.equals(objectId, that.objectId) &&
           Objects.equals(schemeRole, that.schemeRole) &&
           Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectId, schemeRole, permission);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .add("schemeRole", schemeRole)
        .add("permission", permission)
        .toString();
  }

}
