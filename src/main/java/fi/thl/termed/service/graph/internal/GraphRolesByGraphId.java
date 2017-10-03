package fi.thl.termed.service.graph.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class GraphRolesByGraphId extends AbstractSqlSpecification<GraphRole, Empty> {

  private GraphId graphId;

  GraphRolesByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(GraphRole graphRole, Empty value) {
    return Objects.equals(graphRole.getGraphId(), graphId.getId());
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("graph_id = ?", graphId.getId());
  }

}
