package fi.thl.termed.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public final class MultimapUtils {

  private MultimapUtils() {
  }

  public static <K, V> Multimap<K, V> nullToEmpty(Multimap<K, V> map) {
    return map == null ? ImmutableMultimap.<K, V>of() : map;
  }

}
