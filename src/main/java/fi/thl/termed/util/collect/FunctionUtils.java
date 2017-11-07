package fi.thl.termed.util.collect;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class FunctionUtils {

  private FunctionUtils() {
  }

  public static <F, T> Function<F, T> toUnchecked(CheckedFunction<F, T> checked) {
    return input -> {
      try {
        return checked.apply(input);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Apply first parameter of a BiFunction to produce regular one parameter Function
   */
  public static <T, U, R> Function<U, R> partialApply(
      BiFunction<T, U, R> biFunction, T firstParameter) {
    return u -> biFunction.apply(firstParameter, u);
  }

  /**
   * Apply second parameter of a BiFunction to produce regular one parameter Function
   */
  public static <T, U, R> Function<T, R> partialApplySecond(
      BiFunction<T, U, R> biFunction, U secondParameter) {
    return t -> biFunction.apply(t, secondParameter);
  }

  /**
   * Memoize given function with "infinite" cache.
   */
  public static <F, T> Function<F, T> memoize(Function<F, T> function) {
    return CacheBuilder.newBuilder().build(CacheLoader.from(function::apply))::getUnchecked;
  }

  /**
   * Memoize given function with given maximum cache size.
   */
  public static <F, T> Function<F, T> memoize(Function<F, T> function, long maxCacheSize) {
    return CacheBuilder.newBuilder().maximumSize(maxCacheSize)
        .build(CacheLoader.from(function::apply))::getUnchecked;
  }

}
