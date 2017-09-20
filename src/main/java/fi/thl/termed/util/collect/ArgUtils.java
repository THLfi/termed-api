package fi.thl.termed.util.collect;

import static fi.thl.termed.util.ObjectUtils.cast;

import java.util.Map;
import java.util.stream.Stream;

public final class ArgUtils {

  private ArgUtils() {
  }

  public static Map<String, Object> map(Arg[] args) {
    return Stream.of(args).collect(MapUtils.toLinkedHashMap(Arg::getName, Arg::getValue));
  }

  public static <T> T find(Arg[] args, Class<T> type, String name, T defaultValue) {
    return Stream.of(args)
        .filter(arg -> arg.getName().equals(name))
        .findFirst()
        .map(arg -> cast(arg.getValue(), type, defaultValue))
        .orElse(defaultValue);
  }

  public static Boolean findBoolean(Arg[] args, String name, Boolean defaultValue) {
    return find(args, Boolean.class, name, defaultValue);
  }

  public static Boolean findBoolean(Arg[] args, String name) {
    return find(args, Boolean.class, name, false);
  }

}
