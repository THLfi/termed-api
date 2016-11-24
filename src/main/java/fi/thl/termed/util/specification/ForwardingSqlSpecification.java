package fi.thl.termed.util.specification;

import java.io.Serializable;

public class ForwardingSqlSpecification<K extends Serializable, V>
    extends AbstractSqlSpecification<K, V> {

  private SqlSpecification<K, V> delegate;

  public ForwardingSqlSpecification(SqlSpecification<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean test(K k, V v) {
    return delegate.test(k, v);
  }

  @Override
  public String sqlQueryTemplate() {
    return delegate.sqlQueryTemplate();
  }

  @Override
  public Object[] sqlQueryParameters() {
    return delegate.sqlQueryParameters();
  }

}
