package fi.thl.termed.util.specification;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Arrays;
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
    return Objects.equals(sqlQueryTemplate(), that.sqlQueryTemplate()) &&
           Arrays.equals(sqlQueryParameters(), that.sqlQueryParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(sqlQueryTemplate(), Arrays.hashCode(sqlQueryParameters()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("sqlQueryTemplate", sqlQueryTemplate())
        .add("sqlQueryParameters", Arrays.asList(sqlQueryParameters()))
        .toString();
  }

}
