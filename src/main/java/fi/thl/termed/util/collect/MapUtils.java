package fi.thl.termed.util.collect;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

public final class MapUtils {

  private MapUtils() {
  }

  public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
    return map == null ? Collections.<K, V>emptyMap() : map;
  }

  public static <K, V> Map<K, V> toMap(Iterable<V> values, final Function<V, K> keyExtractor) {
    return newLinkedHashMap(Iterables.transform(values, new Function<V, Map.Entry<K, V>>() {
      public Map.Entry<K, V> apply(V input) {
        return simpleEntry(keyExtractor.apply(input), input);
      }
    }));
  }

  public static <K, V> Map.Entry<K, V> simpleEntry(K key, V value) {
    return new AbstractMap.SimpleEntry<K, V>(key, value);
  }

  public static <K, V> Map<K, V> newLinkedHashMap(Iterable<Map.Entry<K, V>> entries) {
    return putEntries(Maps.<K, V>newLinkedHashMap(), entries);
  }

  public static <K, V> Map<K, V> putEntries(Map<K, V> map, Iterable<Map.Entry<K, V>> entries) {
    for (Map.Entry<K, V> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  public static <K, V> Map<K, V> leftValues(Map<K, MapDifference.ValueDifference<V>> diff) {
    return Maps.transformValues(diff, new Function<MapDifference.ValueDifference<V>, V>() {
      @Override
      public V apply(MapDifference.ValueDifference<V> input) {
        return input.leftValue();
      }
    });
  }

  public static <K, V> Map<K, V> rightValues(Map<K, MapDifference.ValueDifference<V>> diff) {
    return Maps.transformValues(diff, new Function<MapDifference.ValueDifference<V>, V>() {
      @Override
      public V apply(MapDifference.ValueDifference<V> input) {
        return input.rightValue();
      }
    });
  }

}
