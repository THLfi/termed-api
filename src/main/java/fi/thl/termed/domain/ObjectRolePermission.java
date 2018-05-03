package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ObjectRolePermission<K extends Serializable> implements Serializable {

  private final K objectId;
  private final GraphRole graphRole;
  private final Permission permission;

  public ObjectRolePermission(K objectId, GraphRole graphRole, Permission permission) {
    this.objectId = checkNotNull(objectId, "objectId can't be null in %s", getClass());
    this.graphRole = checkNotNull(graphRole, "graphRole can't be null in %s", getClass());
    this.permission = checkNotNull(permission, "permission can't be null in %s", getClass());
  }

  public K getObjectId() {
    return objectId;
  }

  public GraphRole getGraphRole() {
    return graphRole;
  }

  public Object getGraph() {
    return graphRole != null ? graphRole.getGraph() : null;
  }

  public UUID getGraphId() {
    return graphRole != null ? graphRole.getGraphId() : null;
  }

  public String getRole() {
    return graphRole != null ? graphRole.getRole() : null;
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
        Objects.equals(graphRole, that.graphRole) &&
        Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectId, graphRole, permission);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .add("graphRole", graphRole)
        .add("permission", permission)
        .toString();
  }

}
