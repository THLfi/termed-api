package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ReferenceAttributeId implements Serializable {

  private final ClassId domainId;

  private final ClassId rangeId;

  private final String id;

  public ReferenceAttributeId(ReferenceAttribute attribute) {
    this(new ClassId(attribute.getDomain()), new ClassId(attribute.getRange()), attribute.getId());
  }

  public ReferenceAttributeId(ClassId domainId, ClassId rangeId, String id) {
    this.domainId = checkNotNull(domainId, "domainId can't be null in %s", getClass());
    this.rangeId = checkNotNull(rangeId, "rangeId can't be null in %s", getClass());
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
  }

  public ClassId getDomainId() {
    return domainId;
  }

  public ClassId getRangeId() {
    return rangeId;
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
    ReferenceAttributeId that = (ReferenceAttributeId) o;
    return Objects.equal(domainId, that.domainId) &&
           Objects.equal(rangeId, that.rangeId) &&
           Objects.equal(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(domainId, rangeId, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("domainId", domainId)
        .add("rangeId", rangeId)
        .add("id", id)
        .toString();
  }

}
