package fi.thl.termed.util.query;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Accepts if any of the contained specifications accept. An empty OrSpecification does not accept
 * anything.
 */
public class OrSpecification<K extends Serializable, V> extends CompositeSpecification<K, V> {

  public OrSpecification() {
  }

  @SafeVarargs
  public OrSpecification(Specification<K, V>... specifications) {
    super(specifications);
  }

  public OrSpecification(List<Specification<K, V>> specifications) {
    super(specifications);
  }

  public OrSpecification<K, V> or(Specification<K, V> specification) {
    specifications.add(checkNotNull(specification));
    return this;
  }

  @Override
  public boolean test(K k, V v) {
    if (specifications.isEmpty()) {
      return false;
    }

    for (Specification<K, V> specification : specifications) {
      if (specification.test(k, v)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery.Builder query = new BooleanQuery.Builder();

    for (Specification<K, V> spec : specifications) {
      query.add(((LuceneSpecification<K, V>) spec).luceneQuery(), BooleanClause.Occur.SHOULD);
    }

    return query.build();
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of(sqlQueryTemplate(), sqlQueryParameters());
  }

  @Override
  public String sqlQueryTemplate() {
    return specifications.isEmpty() ?
        "1 = 0" : specifications.stream()
        .map(SqlSpecification.class::cast)
        .map(spec -> "(" + spec.sqlQueryTemplate() + ")")
        .collect(Collectors.joining(" OR "));
  }

  @Override
  public Object[] sqlQueryParameters() {
    return specifications.stream()
        .map(SqlSpecification.class::cast)
        .flatMap(spec -> stream(spec.sqlQueryParameters()))
        .toArray(Object[]::new);
  }

}
