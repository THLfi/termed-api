package fi.thl.termed.util.collect;

import java.io.Serializable;
import java.util.Objects;

public final class Triple<T1, T2, T3> implements Serializable {

  private final T1 first;
  private final T2 second;
  private final T3 third;

  private Triple(T1 first, T2 second, T3 third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 first, Pair<T2, T3> pair) {
    return new Triple<>(first, pair.getFirst(), pair.getSecond());
  }

  public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 first, T2 second, T3 third) {
    return new Triple<>(first, second, third);
  }

  public T1 getFirst() {
    return first;
  }

  public T2 getSecond() {
    return second;
  }

  public T3 getThird() {
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(first, triple.first) &&
        Objects.equals(second, triple.second) &&
        Objects.equals(third, triple.third);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third);
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ", " + third + ")";
  }

}
