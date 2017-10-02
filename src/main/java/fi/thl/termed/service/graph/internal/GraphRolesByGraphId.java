package fi.thl.termed.service.graph.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import java.util.Objects;

public class GraphRolesByGraphId extends AbstractSqlSpecification<GraphRole, Empty> {

  private GraphId graphId;

  public GraphRolesByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(GraphRole graphRole, Empty value) {
    return Objects.equals(graphRole.getGraphId(), graphId.getId());
  }

  @Override
  public String sqlQueryTemplate() {
    return "graph_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{graphId.getId()};
  }

}
