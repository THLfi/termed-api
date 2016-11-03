package fi.thl.termed.util.specification;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

public class Results<V> {

  private List<V> results;

  private int size;

  public Results(List<V> results, int size) {
    this.results = results;
    this.size = size;
  }

  public List<V> getValues() {
    return results;
  }

  public int getSize() {
    return size;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("results", results)
        .add("size", size)
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
    Results<?> that = (Results<?>) o;
    return Objects.equals(results, that.results) &&
           size == that.size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(results, size);
  }

}
