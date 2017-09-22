package fi.thl.termed.util.query;

import java.io.Serializable;

// In this generic class, we don't implement LuceneSpecification as semantics of
// e.g. MatchAllDocsQuery would not be the same as matching all elements of a certain type.
public class MatchAll<K extends Serializable, V> implements SqlSpecification<K, V> {

  @Override
  public boolean test(K k, V v) {
    return true;
  }

  @Override
  public String sqlQueryTemplate() {
    return "1 = 1";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[0];
  }

}
