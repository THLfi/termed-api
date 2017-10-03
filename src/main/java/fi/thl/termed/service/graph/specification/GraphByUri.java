package fi.thl.termed.service.graph.specification;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class GraphByUri extends AbstractSqlSpecification<GraphId, Graph> {

  private String uri;

  public GraphByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean test(GraphId key, Graph value) {
    return Objects.equals(value.getCode(), uri);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("uri = ?", uri);
  }

}
