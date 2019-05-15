package fi.thl.termed.util;


import static org.junit.jupiter.api.Assertions.assertEquals;

import fi.thl.termed.util.collect.FunctionUtils;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class FunctionUtilsTest {

  @Test
  void shouldPartialApply() {
    BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

    Function<Integer, Integer> addOne = FunctionUtils.partialApply(add, 1);

    assertEquals(32, (int) addOne.apply(31));
    assertEquals(4, (int) addOne.apply(3));
  }

  @Test
  void shouldPartialApplySecond() {
    BiFunction<String, String, String> concatenate = (a, b) -> a + b;

    Function<String, String> append = FunctionUtils.partialApply(concatenate, "|");
    Function<String, String> insert = FunctionUtils.partialApplySecond(concatenate, "|");

    assertEquals("|a", append.apply("a"));
    assertEquals("a|", insert.apply("a"));
  }

  @Test
  void shouldMemoizeFunction() {
    final AtomicInteger counter = new AtomicInteger();

    Function<String, Integer> toIntFunction = str -> {
      counter.incrementAndGet();
      return Integer.valueOf(str);
    };

    assertEquals(0, counter.get());

    toIntFunction.apply("1");
    assertEquals(1, counter.get());

    toIntFunction.apply("1");
    assertEquals(2, counter.get());

    toIntFunction.apply("1");
    assertEquals(3, counter.get());

    Function<String, Integer> memoizedToIntFunction = FunctionUtils.memoize(toIntFunction);

    memoizedToIntFunction.apply("1");
    assertEquals(4, counter.get());

    memoizedToIntFunction.apply("1");
    assertEquals(4, counter.get());

    memoizedToIntFunction.apply("1");
    assertEquals(4, counter.get());
  }

  @Test
  void shouldMemoizeFunctionWithLimitedCache() {
    final AtomicInteger counter = new AtomicInteger();

    Function<String, Integer> toIntFunction = str -> {
      counter.incrementAndGet();
      return Integer.valueOf(str);
    };

    assertEquals(0, counter.get());

    toIntFunction.apply("1");
    assertEquals(1, counter.get());

    toIntFunction.apply("1");
    assertEquals(2, counter.get());

    toIntFunction.apply("1");
    assertEquals(3, counter.get());

    Function<String, Integer> memoizedToIntFunction = FunctionUtils.memoize(toIntFunction, 1);

    memoizedToIntFunction.apply("1");
    assertEquals(4, counter.get());
    memoizedToIntFunction.apply("1");
    assertEquals(4, counter.get());

    // a new value is cached
    memoizedToIntFunction.apply("2");
    assertEquals(5, counter.get());
    memoizedToIntFunction.apply("2");
    assertEquals(5, counter.get());

    // needs to cache "1" again as cache fits only one value
    memoizedToIntFunction.apply("1");
    assertEquals(6, counter.get());
    memoizedToIntFunction.apply("1");
    assertEquals(6, counter.get());
  }

  @Test
  void shouldMemoizeSupplier() {
    final AtomicInteger counter = new AtomicInteger();

    Supplier<String> supplier = () -> {
      counter.incrementAndGet();
      return "foo";
    };

    assertEquals(0, counter.get());

    supplier.get();
    assertEquals(1, counter.get());

    supplier.get();
    assertEquals(2, counter.get());

    Supplier<String> memoizedSupplier = FunctionUtils.memoize(supplier);

    memoizedSupplier.get();
    assertEquals(3, counter.get());

    memoizedSupplier.get();
    assertEquals(3, counter.get());

    memoizedSupplier.get();
    assertEquals(3, counter.get());
  }

}
