package fi.thl.termed.service.type.specification;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TypesById extends AbstractSqlSpecification<TypeId, Type> {

  private String id;

  public TypesById(String id) {
    this.id = id;
  }

  @Override
  public boolean test(TypeId typeId, Type cls) {
    return Objects.equals(typeId.getId(), id);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("id = ?", id);
  }

}
