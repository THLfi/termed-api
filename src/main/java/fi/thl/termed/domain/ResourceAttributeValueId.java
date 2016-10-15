package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceAttributeValueId implements Serializable {

  private final ResourceId resourceId;

  private final String attributeId;

  private final Integer index;

  public ResourceAttributeValueId(ResourceId resourceId,
                                  String attributeId,
                                  Integer index) {
    this.resourceId = checkNotNull(resourceId, "resourceId can't be null in %s", getClass());
    this.attributeId = checkNotNull(attributeId, "attributeId can't be null in s%", getClass());
    this.index = checkNotNull(index, "index can't be null in s%", getClass());
  }

  public ResourceId getResourceId() {
    return resourceId;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public Integer getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceAttributeValueId that = (ResourceAttributeValueId) o;
    return Objects.equal(resourceId, that.resourceId) &&
           Objects.equal(attributeId, that.attributeId) &&
           Objects.equal(index, that.index);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(resourceId, attributeId, index);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("resourceId", resourceId)
        .add("attributeId", attributeId)
        .add("index", index)
        .toString();
  }

}
