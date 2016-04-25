package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;

public class ClassesBySchemeId extends SqlSpecification<ClassId, Class> {

  private UUID schemeId;

  public ClassesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(ClassId classId, Class cls) {
    return Objects.equal(classId.getSchemeId(), schemeId);
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
