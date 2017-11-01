package fi.thl.termed.util.query;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Accepts if all contained specifications accept. An empty AndSpecification does not accept
 * anything.
 */
public final class AndSpecification<K extends Serializable, V> extends
    CompositeSpecification<K, V> {

  private AndSpecification(List<Specification<K, V>> specifications) {
    super(specifications);
  }

  public static <K extends Serializable, V> AndSpecification<K, V> and(
      Specification<K, V> specification) {
    return new AndSpecification<>(singletonList(specification));
  }

  @SafeVarargs
  public static <K extends Serializable, V> AndSpecification<K, V> and(
      Specification<K, V>... specifications) {
    return new AndSpecification<>(asList(specifications));
  }

  public static <K extends Serializable, V> AndSpecification<K, V> and(
      List<Specification<K, V>> specifications) {
    return new AndSpecification<>(specifications);
  }

  @Override
  public boolean test(K k, V v) {
    if (specifications.isEmpty()) {
      return false;
    }

    for (Specification<K, V> specification : specifications) {
      if (!specification.test(k, v)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery.Builder query = new BooleanQuery.Builder();

    for (Specification<K, V> spec : specifications) {
      query.add(((LuceneSpecification<K, V>) spec).luceneQuery(), BooleanClause.Occur.MUST);
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
        "1 = 0" : specifications.stream().map(SqlSpecification.class::cast)
        .map(spec -> "(" + spec.sqlQueryTemplate() + ")")
        .collect(Collectors.joining(" AND "));
  }

  @Override
  public Object[] sqlQueryParameters() {
    return specifications.stream()
        .map(SqlSpecification.class::cast)
        .flatMap(spec -> stream(spec.sqlQueryParameters()))
        .toArray(Object[]::new);
  }

  @Override
  public String toString() {
    return format("(%s)",
        join(" AND ", specifications.stream().map(Object::toString).collect(toList())));
  }

}
