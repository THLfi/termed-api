package fi.thl.termed.util.specification;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

public class NotSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  private Specification<K, V> specification;

  public NotSpecification(Specification<K, V> specification) {
    this.specification = specification;
  }

  @Override
  public boolean test(K k, V v) {
    return !specification.test(k, v);
  }

  public Specification<K, V> getSpecification() {
    return specification;
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery.Builder booleanClauses = new BooleanQuery.Builder();
    booleanClauses.add(new MatchAllDocsQuery(), Occur.SHOULD);
    booleanClauses.add(((LuceneSpecification<K, V>) specification).luceneQuery(), Occur.MUST_NOT);
    return booleanClauses.build();
  }

  @Override
  public String sqlQueryTemplate() {
    return "NOT (" + ((SqlSpecification<K, V>) specification).sqlQueryTemplate() + ")";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return ((SqlSpecification<K, V>) specification).sqlQueryParameters();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotSpecification<?, ?> that = (NotSpecification<?, ?>) o;
    return Objects.equals(specification, that.specification);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(specification);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("specification", specification)
        .toString();
  }

}
