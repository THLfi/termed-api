package fi.thl.termed.util.collect;

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

  public static <K, V> Map.Entry<K, V> newEntry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
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
    return Maps.transformValues(diff, MapDifference.ValueDifference::leftValue);
  }

  public static <K, V> Map<K, V> rightValues(Map<K, MapDifference.ValueDifference<V>> diff) {
    return Maps.transformValues(diff, MapDifference.ValueDifference::rightValue);
  }

}
