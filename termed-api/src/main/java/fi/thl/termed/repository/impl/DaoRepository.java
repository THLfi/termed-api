package fi.thl.termed.repository.impl;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.spesification.SpecificationQuery;

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
  public void save(List<V> values, User user) {
    for (V value : values) {
      save(value, user);
    }
  }

  @Override
  public void save(V value, User user) {
    K key = keyFunction.apply(value);

    if (dao.exists(key, user)) {
      dao.update(key, value, user);
    } else {
      dao.insert(key, value, user);
    }
  }

  @Override
  public void delete(K id, User user) {
    dao.delete(id, user);
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User user) {
    return dao.getValues(specification.getSpecification(), user);
  }

  @Override
  public V get(K id, User user) {
    return dao.get(id, user);
  }

}
