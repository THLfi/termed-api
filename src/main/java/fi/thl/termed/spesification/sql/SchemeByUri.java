package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.util.UUID;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.specification.AbstractSpecification;

public class SchemeByUri extends AbstractSpecification<UUID, Scheme>
    implements SqlSpecification<UUID, Scheme> {

  private String uri;

  public SchemeByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean accept(UUID key, Scheme value) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemeByUri that = (SchemeByUri) o;
    return java.util.Objects.equals(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(uri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("uri", uri)
        .toString();
  }

}
