package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import org.apache.lucene.search.Query;

public class ForwardingLuceneSpecification<K extends Serializable, V>
    implements LuceneSpecification<K, V> {

  private LuceneSpecification<K, V> delegate;

  public ForwardingLuceneSpecification(LuceneSpecification<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean test(K k, V v) {
    return delegate.test(k, v);
  }

  @Override
  public Query luceneQuery() {
    return delegate.luceneQuery();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ForwardingLuceneSpecification<?, ?> that = (ForwardingLuceneSpecification<?, ?>) o;
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
