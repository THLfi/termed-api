package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceId implements Serializable {

  private final UUID schemeId;

  private final String typeId;

  private final UUID id;

  public ResourceId(Resource resource) {
    this(resource.getSchemeId(), resource.getTypeId(), resource.getId());
  }

  /**
   * Construct internal version of an ID from external ID. Throws {@code NullPointerException} if
   * any of the resource key fields is null.
   */
  public ResourceId(ResourceKey resourceKey) {
    this(resourceKey.getSchemeId(), resourceKey.getTypeId(), resourceKey.getId());
  }

  public ResourceId(UUID schemeId, String typeId, UUID id) {
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.typeId = checkNotNull(typeId, "typeId can't be null in %s", getClass());
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  public String getTypeId() {
    return typeId;
  }

  public UUID getId() {
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
    ResourceId that = (ResourceId) o;
    return Objects.equal(schemeId, that.schemeId) &&
           Objects.equal(typeId, that.typeId) &&
           Objects.equal(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId, typeId, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("typeId", typeId)
        .add("id", id)
        .toString();
  }

}
