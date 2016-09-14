package fi.thl.termed.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.Specification;

public interface Dao<K extends Serializable, V> {

  void insert(Map<K, V> map, User user);

  void insert(K key, V value, User user);

  void update(Map<K, V> map, User user);

  void update(K key, V value, User user);

  void delete(List<K> keys, User user);

  void delete(K key, User user);

  Map<K, V> getMap(User user);

  Map<K, V> getMap(Specification<K, V> specification, User user);

  Map<K, V> getMap(List<K> keys, User user);

  List<K> getKeys(User user);

  List<K> getKeys(Specification<K, V> specification, User user);

  List<V> getValues(User user);

  List<V> getValues(Specification<K, V> specification, User user);

  List<V> getValues(List<K> keys, User user);

  boolean exists(K key, User user);

  V get(K key, User user);

}
