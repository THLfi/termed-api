package fi.thl.termed.service.graph.internal;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class GraphPropertiesByGraphId
    extends AbstractSqlSpecification<PropertyValueId<GraphId>, LangValue> {

  private GraphId graphId;

  GraphPropertiesByGraphId(GraphId graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean test(PropertyValueId<GraphId> key, LangValue langValue) {
    return Objects.equals(key.getSubjectId(), graphId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("graph_id = ?", graphId.getId());
  }

}
