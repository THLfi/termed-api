package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public interface Service<K extends Serializable, V> {

  Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user);

  K save(V value, SaveMode mode, WriteOptions opts, User user);

  void delete(Stream<K> keys, WriteOptions opts, User user);

  void delete(K key, WriteOptions opts, User user);

  Stream<K> keys(Query<K, V> query, User user);

  Stream<V> values(Query<K, V> query, User user);

  long count(Specification<K, V> spec, User user);

  boolean exists(K key, User user);

  Optional<V> get(K key, User user, Select... selects);

}
