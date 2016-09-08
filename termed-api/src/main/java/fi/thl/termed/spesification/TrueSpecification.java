package fi.thl.termed.spesification;

import java.io.Serializable;

// In this generic class, we don't implement LuceneSpecification as semantics of
// e.g. MatchAllDocsQuery would not be the same as matching all elements of a certain type.
public class TrueSpecification<K extends Serializable, V> extends AbstractSpecification<K, V>
    implements SqlSpecification<K, V> {

  @Override
  public boolean accept(K key, V value) {
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
