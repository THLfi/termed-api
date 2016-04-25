package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Arrays;

import fi.thl.termed.spesification.Specification;

public abstract class SqlSpecification<K extends Serializable, V> extends Specification<K, V> {

  public abstract String sqlQueryTemplate();

  public abstract Object[] sqlQueryParameters();

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("sqlQueryTemplate", sqlQueryTemplate())
        .add("sqlQueryParameters", sqlQueryParameters())
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SqlSpecification)) {
      return false;
    }

    SqlSpecification<?, ?> that = (SqlSpecification<?, ?>) o;
    return Objects.equal(sqlQueryTemplate(), that.sqlQueryTemplate()) &&
           Arrays.equals(sqlQueryParameters(), that.sqlQueryParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sqlQueryTemplate(), Arrays.hashCode(sqlQueryParameters()));
  }

}
