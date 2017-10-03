package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TypePropertiesByTypeId
    extends AbstractSqlSpecification<PropertyValueId<TypeId>, LangValue> {

  private TypeId typeId;

  TypePropertiesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(PropertyValueId<TypeId> key, LangValue value) {
    return Objects.equals(key.getSubjectId(), typeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("type_graph_id = ? and type_id = ?",
        typeId.getGraphId(), typeId.getId());
  }

}
