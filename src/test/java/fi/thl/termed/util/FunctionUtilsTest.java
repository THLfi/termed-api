package fi.thl.termed.util;

import com.google.common.base.Function;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class FunctionUtilsTest {

  @Test
  public void shouldMemoizeFunction() {
    final AtomicInteger counter = new AtomicInteger();

    Function<String, Integer> toIntFunction = new Function<String, Integer>() {
      @Override
      public Integer apply(String str) {
        counter.incrementAndGet();
        return Integer.valueOf(str);
      }
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
  public void shouldMemoizeFunctionWithLimitedCache() {
    final AtomicInteger counter = new AtomicInteger();

    Function<String, Integer> toIntFunction = new Function<String, Integer>() {
      @Override
      public Integer apply(String str) {
        counter.incrementAndGet();
        return Integer.valueOf(str);
      }
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
  public void shouldPipeFunctions() {
    String testString = "test";

    Function<String, String> appendA = new AppendString("_A");
    Function<String, String> appendB = new AppendString("_B");
    Function<String, String> appendC = new AppendString("_C");
    Function<String, String> appendD = new AppendString("_D");
    Function<String, String> appendE = new AppendString("_E");

    assertEquals("test_A",
                 FunctionUtils.pipe(appendA).apply(testString));
    assertEquals("test_A_B",
                 FunctionUtils.pipe(appendA, appendB).apply(testString));
    assertEquals("test_A_B_C",
                 FunctionUtils.pipe(appendA, appendB, appendC).apply(testString));
    assertEquals("test_A_B_C_D",
                 FunctionUtils.pipe(appendA, appendB, appendC, appendD).apply(testString));
    assertEquals("test_A_B_C_D_E",
                 FunctionUtils.pipe(appendA, appendB, appendC, appendD, appendE).apply(testString));
  }

  private class AppendString implements Function<String, String> {

    private String suffix;

    public AppendString(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public String apply(String input) {
      return input + suffix;
    }
  }

}
