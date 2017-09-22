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
  public String sqlQueryTemplate() {
    if (specifications.isEmpty()) {
      return "1 = 0";
    }

    List<String> query = Lists.newArrayList();

    for (Specification<K, V> spec : specifications) {
      query.add("(" + ((SqlSpecification<K, V>) spec).sqlQueryTemplate() + ")");
    }

    return Joiner.on(" OR ").join(query);
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
