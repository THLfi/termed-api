package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;

public class ReferenceAttributesByClassId
    implements SqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private ClassId classId;

  public ReferenceAttributesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean test(ReferenceAttributeId key, ReferenceAttribute value) {
    return Objects.equals(key.getDomainId(), classId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and domain_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceAttributesByClassId that = (ReferenceAttributesByClassId) o;
    return Objects.equals(classId, that.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(classId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classId", classId)
        .toString();
  }

}
