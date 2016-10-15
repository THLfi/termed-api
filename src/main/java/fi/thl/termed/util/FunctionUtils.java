package fi.thl.termed.util;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public final class FunctionUtils {

  private FunctionUtils() {
  }

  /**
   * Memoize given function with "infinite" cache.
   */
  public static <F, T> Function<F, T> memoize(
      Function<F, T> function) {
    return CacheBuilder.newBuilder().build(CacheLoader.from(function));
  }

  /**
   * Memoize given function with given maximum cache size.
   */
  public static <F, T> Function<F, T> memoize(
      Function<F, T> function, long maxCacheSize) {
    return CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(CacheLoader.from(function));
  }

  /**
   * Just returns the function, defined for completeness.
   */
  public static <T> Function<T, T> pipe(Function<T, T> f1) {
    return f1;
  }

  /**
   * Runs first f1 then f2.
   */
  public static <T> Function<T, T> pipe(final Function<T, T> f1,
                                        final Function<T, T> f2) {
    return new Function<T, T>() {
      public T apply(T input) {
        return f2.apply(f1.apply(input));
      }
    };
  }

  /**
   * Applies functions in given order: f1, f2, f3,..
   */
  public static <T> Function<T, T> pipe(
      final Function<T, T> f1,
      final Function<T, T> f2,
      final Function<T, T> f3) {
    return new Function<T, T>() {
      public T apply(T input) {
        return f3.apply(f2.apply(f1.apply(input)));
      }
    };
  }

  /**
   * Applies functions in given order: f1, f2, f3,..
   */
  public static <T> Function<T, T> pipe(
      final Function<T, T> f1,
      final Function<T, T> f2,
      final Function<T, T> f3,
      final Function<T, T> f4) {
    return new Function<T, T>() {
      public T apply(T input) {
        return f4.apply(f3.apply(f2.apply(f1.apply(input))));
      }
    };
  }

  /**
   * Applies functions in given order: f1, f2, f3,..
   */
  public static <T> Function<T, T> pipe(
      final Function<T, T>... functions) {
    return new Function<T, T>() {
      public T apply(T input) {
        T result = input;

        for (Function<T, T> function : functions) {
          result = function.apply(result);
        }

        return result;
      }
    };
  }

}
