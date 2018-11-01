package fi.thl.termed.util.collect;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

public class LazyLoadingMultimap<K, V> implements ListMultimap<K, V> {

  private final ImmutableSet<K> keys;
  private final Function<K, ImmutableList<V>> valueLoader;

  public LazyLoadingMultimap(ImmutableSet<K> keys,
      Function<K, ImmutableList<V>> valueLoader) {
    this.keys = requireNonNull(keys);
    this.valueLoader = requireNonNull(valueLoader);
  }

  @Override
  public boolean containsKey(Object key) {
    return keys.contains(key);
  }

  @Override
  public boolean containsEntry(Object key, Object value) {
    return get((K) key).contains(value);
  }

  @Override
  public ImmutableList<V> get(K key) {
    return keys.contains(key) ? valueLoader.apply(key) : ImmutableList.of();
  }

  @Override
  public ImmutableSet<K> keySet() {
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
  public ImmutableMultiset<K> keys() {
    ImmutableMultiset.Builder<K> keys = ImmutableMultiset.builder();
    entries().forEach(e -> keys.add(e.getKey()));
    return keys.build();
  }

  @Override
  public ImmutableCollection<V> values() {
    ImmutableList.Builder<V> values = ImmutableList.builder();
    keys.forEach(key -> values.addAll(get(key)));
    return values.build();
  }

  @Override
  public ImmutableCollection<Entry<K, V>> entries() {
    ImmutableList.Builder<Entry<K, V>> entries = ImmutableList.builder();
    keys.forEach(key -> get(key)
        .forEach(value -> entries.add(new SimpleImmutableEntry<>(key, value))));
    return entries.build();
  }

  @Override
  public ImmutableMap<K, Collection<V>> asMap() {
    ImmutableMap.Builder<K, Collection<V>> map = ImmutableMap.builder();
    keys.forEach(key -> map.put(key, get(key)));
    return map.build();
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
