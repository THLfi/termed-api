package fi.thl.termed.service.type.specification;

import java.util.Objects;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

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
  public String sqlQueryTemplate() {
    return "id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{id};
  }

}
