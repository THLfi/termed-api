package fi.thl.termed.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ObjectUtils {

  private ObjectUtils() {
  }

  public static <E> E cast(Object o, Class<E> cls, E defaultValue) {
    return o != null && cls.isAssignableFrom(o.getClass()) ? cls.cast(o) : defaultValue;
  }

  public static Boolean castBoolean(Object o, Boolean defaultValue) {
    return cast(o, Boolean.class, defaultValue);
  }

  public static Integer castInteger(Object o, Integer defaultValue) {
    return cast(o, Integer.class, defaultValue);
  }

  public static List<String> castStringList(Object o) {
    List<String> stringList = new ArrayList<>();

    for (Object item : cast(o, List.class, Collections.emptyList())) {
      Optional.of(castString(item, null)).ifPresent(stringList::add);
    }

    return stringList;
  }

  public static String castString(Object o, String defaultValue) {
    return cast(o, String.class, defaultValue);
  }

  public static <E> E coalesce(E a, E b) {
    return a == null ? b : a;
  }

  public static <E> E coalesce(E a, E b, E c) {
    return a != null ? a : (b != null ? b : c);
  }

  public static <E> E coalesce(E... values) {
    if (values != null) {
      for (E value : values) {
        if (value != null) {
          return value;
        }
      }
    }
    return null;
  }

}
