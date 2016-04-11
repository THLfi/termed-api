package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertyValueId<K extends Serializable> implements Serializable {

  private final K subjectId;

  private final String propertyId;

  private final Integer index;

  public PropertyValueId(K subjectId, String propertyId, Integer index) {
    this.subjectId = checkNotNull(subjectId, "subjectId can't be null in %s", getClass());
    this.propertyId = checkNotNull(propertyId, "propertyId can't be null in %s", getClass());
    this.index = checkNotNull(index, "index can't be null in %s", getClass());
  }

  public K getSubjectId() {
    return subjectId;
  }

  public String getPropertyId() {
    return propertyId;
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
    PropertyValueId<?> that = (PropertyValueId<?>) o;
    return Objects.equal(subjectId, that.subjectId) &&
           Objects.equal(propertyId, that.propertyId) &&
           Objects.equal(index, that.index);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(subjectId, propertyId, index);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subjectId", subjectId)
        .add("propertyId", propertyId)
        .add("index", index)
        .toString();
  }

}
