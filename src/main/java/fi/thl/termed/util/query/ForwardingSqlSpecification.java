package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;

public class ForwardingSqlSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V> {

  private SqlSpecification<K, V> delegate;

  public ForwardingSqlSpecification(SqlSpecification<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean test(K k, V v) {
    return delegate.test(k, v);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return delegate.sql();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ForwardingSqlSpecification<?, ?> that = (ForwardingSqlSpecification<?, ?>) o;
    return Objects.equals(delegate, that.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delegate);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("delegate", delegate)
        .toString();
  }

}
