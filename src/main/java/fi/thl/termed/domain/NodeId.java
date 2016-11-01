package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class NodeId implements Serializable {

  private UUID id;

  private TypeId type;

  public NodeId(Node node) {
    this(node.getId(), node.getTypeId(), node.getTypeGraphId());
  }

  public NodeId(UUID id, String typeId, UUID graphId) {
    this(id, new TypeId(typeId, new GraphId(graphId)));
  }

  public NodeId(UUID id, TypeId type) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.type = checkNotNull(type, "type can't be null in %s", getClass());
  }

  public UUID getId() {
    return id;
  }

  public void setType(TypeId type) {
    this.type = type;
  }

  public TypeId getType() {
    return type;
  }

  public UUID getTypeGraphId() {
    return type != null ? type.getGraphId() : null;
  }

  public String getTypeId() {
    return type != null ? type.getId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeId that = (NodeId) o;
    return Objects.equals(id, that.id) && Objects.equals(type, that.type);

  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("type", type)
        .toString();
  }

}
