package fi.thl.termed.util.dao;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public interface Dao<K extends Serializable, V> {

  void insert(Stream<Tuple2<K, V>> entries, User user);

  void insert(K key, V value, User user);

  void update(Stream<Tuple2<K, V>> entries, User user);

  void update(K key, V value, User user);

  void delete(Stream<K> keys, User user);

  void delete(K key, User user);

  Stream<Tuple2<K, V>> entries(Specification<K, V> specification, User user);

  Stream<K> keys(Specification<K, V> specification, User user);

  Stream<V> values(Specification<K, V> specification, User user);

  boolean exists(K key, User user);

  Optional<V> get(K key, User user);

}
