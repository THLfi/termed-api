package fi.thl.termed.repository.impl;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.spesification.Specification;

/**
 * Repository implementation backed by a Dao. Useful e.g. for testing.
 */
public class DaoRepository<K extends Serializable, V> implements Repository<K, V> {

  private Dao<K, V> dao;
  private Function<V, K> keyFunction;

  public DaoRepository(Dao<K, V> dao, Function<V, K> keyFunction) {
    this.dao = dao;
    this.keyFunction = keyFunction;
  }

  @Override
  public void save(Iterable<V> values) {
    for (V value : values) {
      save(value);
    }
  }

  @Override
  public void save(V value) {
    K key = keyFunction.apply(value);

    if (dao.exists(key)) {
      dao.update(key, value);
    } else {
      dao.insert(key, value);
    }
  }

  @Override
  public void delete(Iterable<K> ids) {
    dao.delete(ids);
  }

  @Override
  public void delete(K id) {
    dao.delete(id);
  }

  @Override
  public boolean exists(K id) {
    return dao.exists(id);
  }

  @Override
  public List<V> get() {
    return dao.getValues();
  }

  @Override
  public List<V> get(Iterable<K> ids) {
    return dao.getValues(ids);
  }

  @Override
  public List<V> get(Specification<K, V> specification) {
    return dao.getValues(specification);
  }

  @Override
  public V get(K id) {
    return dao.get(id);
  }

}
