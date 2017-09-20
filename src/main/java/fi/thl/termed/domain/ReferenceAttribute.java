package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import fi.thl.termed.util.collect.Identifiable;
import java.util.Objects;
import java.util.UUID;

public class ReferenceAttribute extends Attribute implements Identifiable<ReferenceAttributeId> {

  private TypeId range;

  public ReferenceAttribute(String id, TypeId domain, TypeId range) {
    super(id, domain);
    this.range = range;
  }

  public ReferenceAttribute(String id, String uri, TypeId domain, TypeId range) {
    super(id, uri, domain);
    this.range = range;
  }

  public ReferenceAttribute(ReferenceAttribute referenceAttribute) {
    super(referenceAttribute);
    this.range = referenceAttribute.range;
  }

  @Override
  public ReferenceAttributeId identifier() {
    return new ReferenceAttributeId(getDomain(), getId());
  }

  public TypeId getRange() {
    return range;
  }

  public void setRange(TypeId range) {
    this.range = range;
  }

  public UUID getRangeGraphId() {
    return range != null ? range.getGraphId() : null;
  }

  public String getRangeId() {
    return range != null ? range.getId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ReferenceAttribute that = (ReferenceAttribute) o;
    return Objects.equals(range, that.range);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), range);
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("range", range);
  }

}
