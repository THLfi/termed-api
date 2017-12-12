package fi.thl.termed.util.collect;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class LazyLoadingMultimap<K, V> implements ListMultimap<K, V> {

  private final Set<K> keys;
  private final Function<K, List<V>> valueLoader;

  public LazyLoadingMultimap(Set<K> keys, Function<K, List<V>> valueLoader) {
    this.keys = requireNonNull(keys);
    this.valueLoader = requireNonNull(valueLoader);
  }

  @Override
  public boolean containsKey(Object key) {
    return keys.contains(key);
  }

  @Override
  public List<V> get(K key) {
    return keys.contains(key) ? valueLoader.apply(key) : emptyList();
  }

  @Override
  public Set<K> keySet() {
    return keys;
  }

  @Override
  public boolean isEmpty() {
    return keys.isEmpty();
  }

  // rest of the methods require loading all keys

  @Override
  public int size() {
    return entries().size();
  }

  @Override
  public boolean containsValue(Object value) {
    return entries().stream().anyMatch(e -> Objects.equals(value, e.getValue()));
  }

  @Override
  public boolean containsEntry(Object key, Object value) {
    return entries().contains(new SimpleEntry<>(key, value));
  }

  @Override
  public Multiset<K> keys() {
    Multiset<K> keys = LinkedHashMultiset.create();
    entries().forEach(e -> keys.add(e.getKey()));
    return keys;
  }

  @Override
  public Collection<V> values() {
    Collection<V> values = new ArrayList<>();
    keys.forEach(key -> values.addAll(get(key)));
    return values;
  }

  @Override
  public Collection<Entry<K, V>> entries() {
    Collection<Entry<K, V>> entries = new ArrayList<>();
    keys.forEach(key -> get(key)
        .forEach(value -> entries.add(new SimpleEntry<>(key, value))));
    return entries;
  }

  @Override
  public Map<K, Collection<V>> asMap() {
    Map<K, Collection<V>> map = new LinkedHashMap<>();
    keys.forEach(key -> map.put(key, get(key)));
    return map;
  }

  // modifications are not supported

  @Override
  public boolean put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean putAll(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<V> replaceValues(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

}
