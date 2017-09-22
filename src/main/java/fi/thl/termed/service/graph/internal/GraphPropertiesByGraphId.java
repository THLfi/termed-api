package fi.thl.termed.service.graph.internal;

import java.util.Objects;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class GraphPropertiesByGraphId
    extends AbstractSqlSpecification<PropertyValueId<GraphId>, LangValue> {

  private GraphId graphId;

  public GraphPropertiesByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(PropertyValueId<GraphId> key, LangValue langValue) {
    return Objects.equals(key.getSubjectId(), graphId);
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
