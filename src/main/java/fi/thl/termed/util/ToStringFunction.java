package fi.thl.termed.util;

public class ToStringFunction<K> implements java.util.function.Function<K, String> {

  @Override
  public String apply(K input) {
    return input.toString();
  }

}
