package fi.thl.termed.service.scheme.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class SchemeByCode extends AbstractSqlSpecification<UUID, Scheme> {

  private String code;

  public SchemeByCode(String code) {
    this.code = code;
  }

  @Override
  public boolean test(UUID key, Scheme value) {
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
