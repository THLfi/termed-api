package fi.thl.termed.service.graph.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class GraphByCode extends AbstractSqlSpecification<UUID, Graph> {

  private String code;

  public GraphByCode(String code) {
    this.code = code;
  }

  @Override
  public boolean test(UUID key, Graph value) {
    return Objects.equals(value.getCode(), code);
  }

  @Override
  public String sqlQueryTemplate() {
    return "code = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{code};
  }

}
