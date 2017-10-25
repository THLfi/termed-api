package fi.thl.termed.util.collect;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

public final class Pair<T1, T2> implements Serializable {

  private final T1 first;
  private final T2 second;

  private Pair(T1 first, T2 second) {
    this.first = requireNonNull(first);
    this.second = requireNonNull(second);
  }

  public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
    return new Pair<>(first, second);
  }

  public T1 getFirst() {
    return first;
  }

  public T2 getSecond() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(first, pair.first) &&
        Objects.equals(second, pair.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }

}
