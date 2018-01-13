package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;

public class BoostSpecification<K extends Serializable, V> implements LuceneSpecification<K, V> {

  private Specification<K, V> specification;
  private float boost;

  private BoostSpecification(Specification<K, V> specification, float boost) {
    this.specification = specification;
    this.boost = boost;
  }

  public static <K extends Serializable, V> BoostSpecification<K, V> boost(
      Specification<K, V> spec, float boost) {
    return new BoostSpecification<>(spec, boost);
  }

  public Specification<K, V> getSpecification() {
    return specification;
  }

  public float getBoost() {
    return boost;
  }

  @Override
  public boolean test(K k, V v) {
    return specification.test(k, v);
  }

  @Override
  public Query luceneQuery() {
    return new BoostQuery(((LuceneSpecification) specification).luceneQuery(), boost);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("delegate", specification)
        .add("boost", boost)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BoostSpecification<?, ?> that = (BoostSpecification<?, ?>) o;
    return Float.compare(that.boost, boost) == 0 &&
        Objects.equals(specification, that.specification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(specification, boost);
  }

}
