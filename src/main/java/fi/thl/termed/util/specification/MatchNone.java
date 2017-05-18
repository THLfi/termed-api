package fi.thl.termed.util.specification;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.Serializable;

public class MatchNone<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  @Override
  public boolean test(K k, V v) {
    return false;
  }

  @Override
  public Query luceneQuery() {
    return new BooleanQuery.Builder().build();
  }

  @Override
  public String sqlQueryTemplate() {
    return "1 = 0";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[0];
  }

}
