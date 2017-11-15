package fi.thl.termed.util.query;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractSqlSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V> {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SqlSpecification)) {
      return false;
    }

    SqlSpecification<?, ?> that = (SqlSpecification<?, ?>) o;
    return Objects.equals(sql(), that.sql());
  }

  @Override
  public int hashCode() {
    return sql().hashCode();
  }

  /**
   * This is for debug printing only, not safe to run against db
   */
  @Override
  public String toString() {
    String sqlTemplate = sqlQueryTemplate();
    String result = "";

    for (Object queryParameter : sqlQueryParameters()) {
      result = sqlTemplate.replace("?", queryParameter.toString());
    }

    return result;
  }

}
