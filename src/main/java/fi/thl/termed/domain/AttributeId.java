package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;

public abstract class AttributeId implements Serializable {

  private final TypeId domainId;

  private final String id;

  AttributeId(Attribute attribute) {
    this(attribute.getDomain(), attribute.getId());
  }

  AttributeId(TypeId domainId, String id) {
    this.domainId = checkNotNull(domainId, "domainId can't be null in %s", getClass());
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
  }

  public final TypeId getDomainId() {
    return domainId;
  }

  public final String getId() {
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
    AttributeId that = (AttributeId) o;
    return Objects.equals(domainId, that.domainId) &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domainId, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("domainId", domainId)
        .add("id", id)
        .toString();
  }

}
