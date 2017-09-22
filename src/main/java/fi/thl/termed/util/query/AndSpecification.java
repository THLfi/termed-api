package fi.thl.termed.util.query;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Accepts if all contained specifications accept. An empty AndSpecification does not accept
 * anything.
 */
public class AndSpecification<K extends Serializable, V> extends CompositeSpecification<K, V> {

  public AndSpecification() {
  }

  @SafeVarargs
  public AndSpecification(Specification<K, V>... specifications) {
    super(specifications);
  }

  public AndSpecification(List<Specification<K, V>> specifications) {
    super(specifications);
  }

  public AndSpecification<K, V> and(Specification<K, V> specification) {
    specifications.add(checkNotNull(specification));
    return this;
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
  public String sqlQueryTemplate() {
    if (specifications.isEmpty()) {
      return "1 = 0";
    }

    List<String> queryTemplates = Lists.newArrayList();

    for (Specification<K, V> spec : specifications) {
      queryTemplates.add("(" + ((SqlSpecification<K, V>) spec).sqlQueryTemplate() + ")");
    }

    return Joiner.on(" AND ").join(queryTemplates);
  }

  @Override
  public Object[] sqlQueryParameters() {
    checkState(specifications.stream().allMatch(s -> s instanceof SqlSpecification));

    List<Object> queryParameters = Lists.newArrayList();

    for (Specification<K, V> spec : specifications) {
      queryParameters.addAll(Arrays.asList(((SqlSpecification<K, V>) spec).sqlQueryParameters()));
    }

    return queryParameters.toArray(new Object[queryParameters.size()]);
  }

}
