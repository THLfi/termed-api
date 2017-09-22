package fi.thl.termed.util.query;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class CompositeSpecification<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V>, Iterable<Specification<K, V>> {

  List<Specification<K, V>> specifications;

  CompositeSpecification() {
    this(new ArrayList<>());
  }

  @SafeVarargs
  CompositeSpecification(Specification<K, V>... specifications) {
    this(Arrays.asList(specifications));
  }

  CompositeSpecification(List<Specification<K, V>> specifications) {
    this.specifications = checkNotNull(specifications);
  }

  void addSpecification(Specification<K, V> specification) {
    specifications.add(specification);
  }

  public List<Specification<K, V>> getSpecifications() {
    return specifications;
  }

  @Override
  public Iterator<Specification<K, V>> iterator() {
    return specifications.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompositeSpecification<?, ?> that = (CompositeSpecification<?, ?>) o;
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
