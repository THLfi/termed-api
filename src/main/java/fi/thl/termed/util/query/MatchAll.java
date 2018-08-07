package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.io.Serializable;

// In this generic class, we don't implement LuceneSpecification as semantics of
// e.g. MatchAllDocsQuery would not be the same as matching all elements of a certain type.
public class MatchAll<K extends Serializable, V> implements SqlSpecification<K, V> {

  @Override
  public boolean test(K k, V v) {
    return true;
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("1 = 1");
  }

  @Override
  public int hashCode() {
    return MatchAll.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof MatchAll;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

}
