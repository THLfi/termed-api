package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.UUID;

public class ReferenceAttribute extends Attribute {

  private Class range;

  public ReferenceAttribute(Class domain, Class range, String id) {
    super(domain, id);
    this.range = range;
  }

  public ReferenceAttribute(Class domain, Class range, String id, String uri) {
    super(domain, id);
    this.range = range;
    this.setUri(uri);
  }

  public ReferenceAttribute(ReferenceAttribute referenceAttribute) {
    super(referenceAttribute);
    this.range = referenceAttribute.range;
  }

  public Class getRange() {
    return range;
  }

  public void setRange(Class range) {
    this.range = range;
  }

  public ClassId getRangeClassId() {
    return new ClassId(range);
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
    return Objects.equal(getRangeClassId(), that.getRangeClassId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), getRangeClassId());
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("rangeId", getRangeClassId());
  }

}
