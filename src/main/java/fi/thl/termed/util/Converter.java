package fi.thl.termed.util;

import java.util.function.Function;

public abstract class Converter<F, T> implements Function<F, T> {

  public abstract T apply(F f);

  public abstract F applyInverse(T t);

  public Function<T, F> inverse() {
    return this::applyInverse;
  }

}
