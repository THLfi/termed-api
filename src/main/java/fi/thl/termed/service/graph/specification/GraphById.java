package fi.thl.termed.service.graph.specification;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;
import java.util.UUID;

public class GraphById extends AbstractSqlSpecification<GraphId, Graph> {

  private UUID id;

  public GraphById(UUID id) {
    this.id = id;
  }

  @Override
  public boolean test(GraphId key, Graph value) {
    return Objects.equals(value.getId(), id);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("id = ?", id);
  }

}
