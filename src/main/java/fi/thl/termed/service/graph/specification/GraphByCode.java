package fi.thl.termed.service.graph.specification;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class GraphByCode extends AbstractSqlSpecification<GraphId, Graph> {

  private String code;

  public GraphByCode(String code) {
    this.code = code;
  }

  @Override
  public boolean test(GraphId key, Graph value) {
    return Objects.equals(value.getCode(), code);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("code = ?", code);
  }

}
