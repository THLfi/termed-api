package fi.thl.termed.util;

import com.google.common.base.Function;

public class ToStringFunction<K> implements Function<K, String> {

  @Override
  public String apply(K input) {
    return input.toString();
  }

}
