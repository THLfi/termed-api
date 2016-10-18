package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class ClassPropertiesByClassId
    extends AbstractSqlSpecification<PropertyValueId<ClassId>, LangValue> {

  private ClassId classId;

  public ClassPropertiesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean test(PropertyValueId<ClassId> key, LangValue value) {
    return Objects.equals(key.getSubjectId(), classId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "class_scheme_id = ? and class_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId()};
  }

}
