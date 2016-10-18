package fi.thl.termed.service.scheme.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemeByUri extends AbstractSqlSpecification<UUID, Scheme> {

  private String uri;

  public SchemeByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean test(UUID key, Scheme value) {
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
