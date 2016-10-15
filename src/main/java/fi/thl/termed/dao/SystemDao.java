package fi.thl.termed.dao;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.spesification.Specification;

/**
 * Lower level api to handle object persisting. Typically there is one Dao for each persisted type.
 * Database backed DAOs should operate on one table only. Repositories should handle more complex
 * object graphs using DAOs and other Repositories.
 */
public interface SystemDao<K extends Serializable, V> {

  void insert(Map<K, V> map);

  void insert(K key, V value);

  void update(Map<K, V> map);

  void update(K key, V value);

  void delete(List<K> keys);

  void delete(K key);

  Map<K, V> getMap();

  Map<K, V> getMap(Specification<K, V> specification);

  Map<K, V> getMap(List<K> keys);

  List<K> getKeys();

  List<K> getKeys(Specification<K, V> specification);

  List<V> getValues();

  List<V> getValues(Specification<K, V> specification);

  List<V> getValues(List<K> keys);

  boolean exists(K key);

  Optional<V> get(K key);

}
