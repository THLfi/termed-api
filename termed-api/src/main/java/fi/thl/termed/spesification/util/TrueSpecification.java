package fi.thl.termed.spesification.util;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.spesification.SqlSpecification;

// In this generic class, we don't implement LuceneSpecification as semantics of
// e.g. MatchAllDocsQuery would not be the same as matching all elements of a certain type.
public class TrueSpecification<K extends Serializable, V> implements SqlSpecification<K, V> {

  @Override
  public boolean apply(Map.Entry<K, V> entry) {
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
