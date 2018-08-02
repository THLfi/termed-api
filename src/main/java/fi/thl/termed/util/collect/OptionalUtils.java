package fi.thl.termed.util.collect;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class OptionalUtils {

  private OptionalUtils() {
  }

  /**
   * Returns the first optional that contains a value or an empty optional.
   */
  public static <T> Optional<T> findFirst(Stream<Optional<T>> optionals) {
    return optionals
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  public static <T> Optional<T> findFirst(Optional<T> o0) {
    return findFirst(Stream.of(o0));
  }

  public static <T> Optional<T> findFirst(Optional<T> o0, Optional<T> o1) {
    return findFirst(Stream.of(o0, o1));
  }

  public static <T> Optional<T> findFirst(Optional<T> o0, Optional<T> o1, Optional<T> o2) {
    return findFirst(Stream.of(o0, o1, o2));
  }

  public static <T> Optional<T> findFirst(Optional<T> o0, Optional<T> o1, Optional<T> o2,
      Optional<T> o3) {
    return findFirst(Stream.of(o0, o1, o2, o3));
  }

  /**
   * Lazy evaluating version that returns the first optional that contains a value or an empty
   * optional.
   */
  public static <T> Optional<T> lazyFindFirst(Stream<Supplier<Optional<T>>> optionals) {
    return optionals
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  public static <T> Optional<T> lazyFindFirst(Supplier<Optional<T>> o0) {
    return lazyFindFirst(Stream.of(o0));
  }

  public static <T> Optional<T> lazyFindFirst(Supplier<Optional<T>> o0, Supplier<Optional<T>> o1) {
    return lazyFindFirst(Stream.of(o0, o1));
  }

  public static <T> Optional<T> lazyFindFirst(Supplier<Optional<T>> o0, Supplier<Optional<T>> o1,
      Supplier<Optional<T>> o2) {
    return lazyFindFirst(Stream.of(o0, o1, o2));
  }

  public static <T> Optional<T> lazyFindFirst(Supplier<Optional<T>> o0, Supplier<Optional<T>> o1,
      Supplier<Optional<T>> o2, Supplier<Optional<T>> o3) {
    return lazyFindFirst(Stream.of(o0, o1, o2, o3));
  }

}
