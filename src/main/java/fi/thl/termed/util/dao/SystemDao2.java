package fi.thl.termed.util.dao;

import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public interface SystemDao2<K extends Serializable, V> {

  void insert(Stream<Tuple2<K, V>> map);

  void insert(K key, V value);

  void update(Stream<Tuple2<K, V>> map);

  void update(K key, V value);

  void delete(Stream<K> keys);

  void delete(K key);

  Stream<Tuple2<K, V>> getEntries(Specification<K, V> specification);

  Stream<K> getKeys(Specification<K, V> specification);

  Stream<V> getValues(Specification<K, V> specification);

  boolean exists(K key);

  Optional<V> get(K key);

}
