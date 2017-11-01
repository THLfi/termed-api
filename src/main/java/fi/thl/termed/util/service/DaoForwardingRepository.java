package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Can be used to create simple service backed by a single DAO.
 */
public class DaoForwardingRepository<K extends Serializable, V extends Identifiable<K>>
    extends AbstractRepository<K, V> {

  private Dao<K, V> delegate;

  public DaoForwardingRepository(Dao<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected void insert(K id, V value, SaveMode mode, WriteOptions opts, User user) {
    delegate.insert(id, value, user);
  }

  @Override
  protected void update(K id, V value, SaveMode mode, WriteOptions opts, User user) {
    delegate.update(id, value, user);
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    delegate.delete(id, user);
  }

  @Override
  public List<V> getValues(User user) {
    return delegate.getValues(user);
  }

  @Override
  public List<V> getValues(Specification<K, V> spec, User user) {
    return delegate.getValues(spec, user);
  }

  @Override
  public List<V> getValues(Query<K, V> query, User user) {
    return delegate.getValues(query.getWhere(), user);
  }

  @Override
  public Stream<V> getValueStream(User user) {
    return delegate.getValues(user).stream();
  }

  @Override
  public Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return delegate.getValues(spec, user).stream();
  }

  @Override
  public Stream<V> getValueStream(Query<K, V> query, User user) {
    return delegate.getValues(query.getWhere(), user).stream();
  }

  @Override
  public List<K> getKeys(User user) {
    return delegate.getKeys(user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> spec, User user) {
    return delegate.getKeys(spec, user);
  }

  @Override
  public List<K> getKeys(Query<K, V> query, User user) {
    return delegate.getKeys(query.getWhere(), user);
  }

  @Override
  public Stream<K> getKeyStream(User user) {
    return delegate.getKeys(user).stream();
  }

  @Override
  public Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return delegate.getKeys(spec, user).stream();
  }

  @Override
  public Stream<K> getKeyStream(Query<K, V> query, User user) {
    return delegate.getKeys(query.getWhere(), user).stream();
  }

  @Override
  public boolean exists(K key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public Optional<V> get(K id, User user, Select... selects) {
    return delegate.get(id, user);
  }

}
