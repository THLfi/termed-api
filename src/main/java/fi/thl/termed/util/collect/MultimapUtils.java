package fi.thl.termed.util.collect;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

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

  public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableMultimap<K, V>> toImmutableMultimap() {
    return Collector.of(
        ImmutableMultimap.Builder::new,
        ImmutableMultimap.Builder::put,
        (l, r) -> l.putAll(r.build()),
        (Function<ImmutableMultimap.Builder<K, V>, ImmutableMultimap<K, V>>) ImmutableMultimap.Builder::build);
  }

  public static <T, K, V> Collector<T, ?, ImmutableMultimap<K, V>> toImmutableMultimap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends V> valueMapper) {
    return Collector.of(
        ImmutableMultimap.Builder::new,
        (b, t) -> b.put(keyMapper.apply(t), valueMapper.apply(t)),
        (l, r) -> l.putAll(r.build()),
        (Function<ImmutableMultimap.Builder<K, V>, ImmutableMultimap<K, V>>) ImmutableMultimap.Builder::build);
  }

  public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends V> valueMapper) {
    return Collector.of(
        ImmutableListMultimap.Builder::new,
        (b, t) -> b.put(keyMapper.apply(t), valueMapper.apply(t)),
        (l, r) -> l.putAll(r.build()),
        (Function<ImmutableListMultimap.Builder<K, V>, ImmutableListMultimap<K, V>>) ImmutableListMultimap.Builder::build);
  }

}
