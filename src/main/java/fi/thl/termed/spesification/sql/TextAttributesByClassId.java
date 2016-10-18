package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;

public class TextAttributesByClassId
    implements SqlSpecification<TextAttributeId, TextAttribute> {

  private ClassId classId;

  public TextAttributesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean test(TextAttributeId key, TextAttribute value) {
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
    TextAttributesByClassId that = (TextAttributesByClassId) o;
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
