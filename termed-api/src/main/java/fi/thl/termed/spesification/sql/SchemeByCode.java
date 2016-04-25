package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import java.util.UUID;

import fi.thl.termed.domain.Scheme;

public class SchemeByCode extends SqlSpecification<UUID, Scheme> {

  private String code;

  public SchemeByCode(String code) {
    this.code = code;
  }

  @Override
  public boolean accept(UUID key, Scheme value) {
    return Objects.equal(value.getCode(), code);
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
