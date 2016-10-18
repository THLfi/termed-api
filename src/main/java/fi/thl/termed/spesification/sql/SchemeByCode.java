package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.specification.SqlSpecification;

public class SchemeByCode implements SqlSpecification<UUID, Scheme> {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemeByCode that = (SchemeByCode) o;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(code);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("code", code)
        .toString();
  }

}
