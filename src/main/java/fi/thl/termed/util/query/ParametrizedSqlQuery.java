package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Objects;

public class ParametrizedSqlQuery {

  private final String queryTemplate;
  private final Object[] queryParameters;

  private ParametrizedSqlQuery(String queryTemplate, Object... queryParameters) {
    this.queryTemplate = queryTemplate;
    this.queryParameters = queryParameters;
  }

  public static ParametrizedSqlQuery of(String queryTemplate, Object... queryParameters) {
    return new ParametrizedSqlQuery(queryTemplate, queryParameters);
  }

  public String getQueryTemplate() {
    return queryTemplate;
  }

  public Object[] getQueryParameters() {
    return queryParameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParametrizedSqlQuery that = (ParametrizedSqlQuery) o;
    return Objects.equals(queryTemplate, that.queryTemplate) &&
        Arrays.equals(queryParameters, that.queryParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryTemplate, Arrays.hashCode(queryParameters));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("queryTemplate", queryTemplate)
        .add("queryParameters", Arrays.asList(queryParameters))
        .toString();
  }

}
