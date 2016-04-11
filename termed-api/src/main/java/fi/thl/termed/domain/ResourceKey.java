package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

/**
 * Relaxed version of a resource identifier to be used in e.g. controllers. In internal API,
 * ResourceId is used.
 */
public class ResourceKey implements Serializable {

  private UUID schemeId;

  private String typeId;

  private UUID id;

  public ResourceKey() {
  }

  public ResourceKey(UUID schemeId, String typeId, UUID id) {
    this.schemeId = schemeId;
    this.typeId = typeId;
    this.id = id;
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  public void setSchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  public String getTypeId() {
    return typeId;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceKey that = (ResourceKey) o;
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
