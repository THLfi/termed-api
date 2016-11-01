package fi.thl.termed.service.graph.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class GraphByUri extends AbstractSqlSpecification<UUID, Graph> {

  private String uri;

  public GraphByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean test(UUID key, Graph value) {
    return Objects.equals(value.getCode(), uri);
  }

  @Override
  public String sqlQueryTemplate() {
    return "uri = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{uri};
  }

}
