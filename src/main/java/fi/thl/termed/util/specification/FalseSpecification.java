package fi.thl.termed.util.specification;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class FalseSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  @Override
  public boolean apply(Map.Entry<K, V> entry) {
    return false;
  }

  @Override
  public Query luceneQuery() {
    return new BooleanQuery();
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
