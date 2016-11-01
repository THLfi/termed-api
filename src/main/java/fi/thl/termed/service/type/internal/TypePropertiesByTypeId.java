package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TypePropertiesByTypeId
    extends AbstractSqlSpecification<PropertyValueId<TypeId>, LangValue> {

  private TypeId typeId;

  public TypePropertiesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(PropertyValueId<TypeId> key, LangValue value) {
    return Objects.equals(key.getSubjectId(), typeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "type_graph_id = ? and type_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
  }

}
