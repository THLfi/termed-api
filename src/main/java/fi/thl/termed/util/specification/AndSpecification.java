package fi.thl.termed.util.specification;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Accepts if all contained specifications accept. An empty AndSpecification does not accept
 * anything.
 */
public class AndSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  private List<Specification<K, V>> specifications;

  public AndSpecification() {
    this(new ArrayList<>());
  }

  public AndSpecification(Specification<K, V> s1,
                          Specification<K, V> s2) {
    this(Arrays.asList(s1, s2));
  }

  public AndSpecification(Specification<K, V> s1,
                          Specification<K, V> s2,
                          Specification<K, V> s3) {
    this(Arrays.asList(s1, s2, s3));
  }

  public AndSpecification(List<Specification<K, V>> specifications) {
    this.specifications = checkNotNull(specifications);
  }

  public AndSpecification<K, V> and(Specification<K, V> specification) {
    specifications.add(checkNotNull(specification));
    return this;
  }

  public List<Specification<K, V>> getSpecifications() {
    return specifications;
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
    if (specifications.isEmpty()) {
      return new BooleanQuery();
    }

    BooleanQuery query = new BooleanQuery();

    for (Specification<K, V> spec : specifications) {
      query.add(((LuceneSpecification<K, V>) spec).luceneQuery(), BooleanClause.Occur.MUST);
    }

    return query;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AndSpecification<?, ?> that = (AndSpecification<?, ?>) o;
    return Objects.equals(specifications, that.specifications);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(specifications);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("specifications", specifications)
        .toString();
  }

}
