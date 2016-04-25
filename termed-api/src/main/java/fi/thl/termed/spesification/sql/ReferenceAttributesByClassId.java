package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;

public class ReferenceAttributesByClassId
    extends SqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private ClassId classId;

  public ReferenceAttributesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean accept(ReferenceAttributeId key, ReferenceAttribute value) {
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
