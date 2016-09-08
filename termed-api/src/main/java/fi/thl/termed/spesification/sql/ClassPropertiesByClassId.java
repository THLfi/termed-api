package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.util.LangValue;

public class ClassPropertiesByClassId
    extends AbstractSpecification<PropertyValueId<ClassId>, LangValue>
    implements SqlSpecification<PropertyValueId<ClassId>, LangValue> {

  private ClassId classId;

  public ClassPropertiesByClassId(ClassId classId) {
    this.classId = classId;
  }

  @Override
  public boolean accept(PropertyValueId<ClassId> key, LangValue value) {
    return Objects.equal(key.getSubjectId(), classId);
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
