package fi.thl.termed.service.scheme.specification;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TextAttributesByClassId
    extends AbstractSqlSpecification<TextAttributeId, TextAttribute> {

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

}
