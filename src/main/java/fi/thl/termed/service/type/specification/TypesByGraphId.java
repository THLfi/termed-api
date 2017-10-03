package fi.thl.termed.service.type.specification;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;
import java.util.UUID;

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
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("graph_id = ?", graphId);
  }

}
