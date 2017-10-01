package fi.thl.termed.util.collect;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public final class MultimapUtils {

  private MultimapUtils() {
  }

  public static <K, V> Multimap<K, V> nullToEmpty(Multimap<K, V> map) {
    return map == null ? ImmutableMultimap.<K, V>of() : map;
  }

  public static <K, V> ImmutableMultimap<K, V> nullToEmpty(ImmutableMultimap<K, V> map) {
    return map == null ? ImmutableMultimap.<K, V>of() : map;
  }

  public static <K, V> ImmutableMultimap<K, V> nullableImmutableCopyOf(Multimap<K, V> map) {
    return map != null ? ImmutableMultimap.<K, V>copyOf(map) : null;
  }

}
