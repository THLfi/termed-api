package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.UUID;

public class ReferenceAttribute extends Attribute implements Identifiable<ReferenceAttributeId> {

  private ClassId range;

  public ReferenceAttribute(String id, ClassId domain, ClassId range) {
    super(id, domain);
    this.range = range;
  }

  public ReferenceAttribute(String id, String uri, ClassId domain, ClassId range) {
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

  public ClassId getRange() {
    return range;
  }

  public void setRange(ClassId range) {
    this.range = range;
  }

  public UUID getRangeSchemeId() {
    return range != null ? range.getSchemeId() : null;
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
