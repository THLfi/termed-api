package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;

public class TextAttributesByClassId
    extends SqlSpecification<TextAttributeId, TextAttribute> {

  private ClassId classId;

  public TextAttributesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean accept(TextAttributeId key, TextAttribute value) {
    return Objects.equal(key.getDomainId(), classId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and domain_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId()};
  }

}
