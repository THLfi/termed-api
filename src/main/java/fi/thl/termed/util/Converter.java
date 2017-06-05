package fi.thl.termed.util;

import java.util.function.Function;

public abstract class Converter<F, T> implements Function<F, T> {

  public static <F, T> Converter<F, T> newConverter(
      Function<F, T> function, Function<T, F> inverse) {
    return new Converter<F, T>() {
      @Override
      public T apply(F f) {
        return function.apply(f);
      }

      @Override
      public F applyInverse(T t) {
        return inverse.apply(t);
      }
    };
  }

  public abstract T apply(F f);

  public abstract F applyInverse(T t);

  public Function<T, F> inverse() {
    return this::applyInverse;
  }

}
