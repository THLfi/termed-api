package fi.thl.termed.repository.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.repository.spesification.Specification;

/**
 * Lower level api to handle object persisting. Typically there is one Dao for each persisted type.
 * Database backed DAOs should operate on one table only. Repositories should handle more complex
 * object graphs using DAOs and other Repositories.
 */
public interface Dao<K extends Serializable, V> {

  void insert(Map<K, V> map);

  void insert(K key, V value);

  void update(Map<K, V> map);

  void update(K key, V value);

  void delete();

  void delete(Iterable<K> keys);

  void delete(K key);

  Map<K, V> getMap();

  Map<K, V> getMap(Specification<K, V> specification);

  Map<K, V> getMap(Iterable<K> keys);

  List<K> getKeys();

  List<K> getKeys(Specification<K, V> specification);

  List<V> getValues();

  List<V> getValues(Specification<K, V> specification);

  List<V> getValues(Iterable<K> keys);

  boolean exists(K key);

  V get(K key);

}
