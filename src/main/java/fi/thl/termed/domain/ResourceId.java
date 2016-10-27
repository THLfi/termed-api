package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceId implements Serializable {

  private UUID id;

  private ClassId type;

  public ResourceId(Resource resource) {
    this(resource.getId(), resource.getTypeId(), resource.getTypeSchemeId());
  }

  public ResourceId(UUID id, String typeId, UUID schemeId) {
    this(id, new ClassId(typeId, new SchemeId(schemeId)));
  }

  public ResourceId(UUID id, ClassId type) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.type = checkNotNull(type, "type can't be null in %s", getClass());
  }

  public UUID getId() {
    return id;
  }

  public void setType(ClassId type) {
    this.type = type;
  }

  public ClassId getType() {
    return type;
  }

  public UUID getTypeSchemeId() {
    return type != null ? type.getSchemeId() : null;
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
    ResourceId that = (ResourceId) o;
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
