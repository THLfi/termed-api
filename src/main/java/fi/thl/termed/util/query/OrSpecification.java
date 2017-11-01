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
 * Accepts if any of the contained specifications accept. An empty OrSpecification does not accept
 * anything.
 */
public final class OrSpecification<K extends Serializable, V> extends CompositeSpecification<K, V> {

  private OrSpecification(List<Specification<K, V>> specifications) {
    super(specifications);
  }

  public static <K extends Serializable, V> OrSpecification<K, V> or(
      Specification<K, V> specification) {
    return new OrSpecification<>(singletonList(specification));
  }

  @SafeVarargs
  public static <K extends Serializable, V> OrSpecification<K, V> or(
      Specification<K, V>... specifications) {
    return new OrSpecification<>(asList(specifications));
  }

  public static <K extends Serializable, V> OrSpecification<K, V> or(
      List<Specification<K, V>> specifications) {
    return new OrSpecification<>(specifications);
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

  @Override
  public String toString() {
    return format("(%s)",
        join(" OR ", specifications.stream().map(Object::toString).collect(toList())));
  }

}
