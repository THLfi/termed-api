package fi.thl.termed.util.query;

import java.io.Serializable;
import java.util.Objects;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

public final class NotSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  private final Specification<K, V> specification;

  private NotSpecification(Specification<K, V> specification) {
    this.specification = specification;
  }

  public static <K extends Serializable, V> NotSpecification<K, V> not(
      Specification<K, V> specification) {
    return new NotSpecification<>(specification);
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
  public ParametrizedSqlQuery sql() {
    SqlSpecification<K, V> sqlSpecification = ((SqlSpecification<K, V>) specification);
    return ParametrizedSqlQuery.of(
        "NOT (" + sqlSpecification.sqlQueryTemplate() + ")",
        sqlSpecification.sqlQueryParameters());
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
    return "NOT " + specification.toString();
  }

}
