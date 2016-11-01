package fi.thl.termed.service.type.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TypesByGraphId extends AbstractSqlSpecification<TypeId, Type> {

  private UUID graphId;

  public TypesByGraphId(UUID graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(TypeId typeId, Type cls) {
    return Objects.equals(typeId.getGraphId(), graphId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "graph_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{graphId};
  }

}
