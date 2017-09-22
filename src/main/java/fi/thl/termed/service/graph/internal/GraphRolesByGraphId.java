package fi.thl.termed.service.graph.internal;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class GraphRolesByGraphId extends AbstractSqlSpecification<GraphRole, Empty> {

  private UUID graphId;

  public GraphRolesByGraphId(UUID graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(GraphRole graphRole, Empty value) {
    return Objects.equals(graphRole.getGraphId(), graphId);
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
