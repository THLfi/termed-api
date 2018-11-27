package fi.thl.termed.util.dao;

import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Simple memory based dao implementation. Useful e.g. in tests.
 */
public class MemoryBasedSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private Map<K, V> data;

  public MemoryBasedSystemDao() {
    this(new LinkedHashMap<>());
  }

  public MemoryBasedSystemDao(Map<K, V> data) {
    this.data = data;
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> stream) {
    try (Stream<Tuple2<K, V>> closeable = stream) {
      closeable.forEach(t -> insert(t._1, t._2));
    }
  }

  @Override
  public void insert(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void update(Stream<Tuple2<K, V>> stream) {
    try (Stream<Tuple2<K, V>> closeable = stream) {
      closeable.forEach(t -> update(t._1, t._2));
    }
  }

  @Override
  public void update(K key, V value) {
    data.put(key, value);
  }

  @Override
  public void delete(Stream<K> keys) {
    try (Stream<K> closeable = keys) {
      closeable.forEach(this::delete);
    }
  }

  @Override
  public void delete(K key) {
    data.remove(key);
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification) {
    return data.entrySet().stream()
        .map(Tuple::of)
        .filter(t -> specification.test(t._1, t._2));
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification) {
    return entries(specification).map(e -> e._1);
  }

  @Override
  public Stream<V> values(Specification<K, V> specification) {
    return entries(specification).map(e -> e._2);
  }

  @Override
  public boolean exists(K key) {
    return data.containsKey(key);
  }

  @Override
  public Optional<V> get(K key) {
    return Optional.ofNullable(data.get(key));
  }

}
