package fi.thl.termed.util;

public final class ObjectUtils {

  private ObjectUtils() {
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
