package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClassId implements Serializable {

  private final UUID schemeId;

  private final String id;

  public ClassId(Resource resource) {
    this(resource.getSchemeId(), resource.getTypeId());
  }

  public ClassId(ResourceId resourceId) {
    this(resourceId.getSchemeId(), resourceId.getTypeId());
  }

  public ClassId(Class cls) {
    this(cls.getSchemeId(), cls.getId());
  }

  public ClassId(UUID schemeId, String id) {
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassId classId = (ClassId) o;
    return Objects.equal(schemeId, classId.schemeId) &&
           Objects.equal(id, classId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("id", id)
        .toString();
  }

}
