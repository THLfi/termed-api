package fi.thl.termed.exchange.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.Map;

public final class ArgsValidator {

  private ArgsValidator() {
  }

  public static boolean validate(Map<String, Object> args, Map<String, Class> requiredArgs) {
    return Maps.difference(Maps.transformValues(args, new Function<Object, Class>() {
      public Class apply(Object argValue) {
        return argValue != null ? argValue.getClass() : null;
      }
    }), requiredArgs).areEqual();
  }

}
