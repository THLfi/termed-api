package fi.thl.termed.service.scheme.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ClassesBySchemeId extends AbstractSqlSpecification<ClassId, Class> {

  private UUID schemeId;

  public ClassesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(ClassId classId, Class cls) {
    return Objects.equals(classId.getSchemeId(), schemeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId};
  }

}
