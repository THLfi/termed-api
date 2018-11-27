package fi.thl.termed.util.dao;

import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public class ForwardingSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private SystemDao<K, V> delegate;

  public ForwardingSystemDao(SystemDao<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    delegate.insert(entries);
  }

  @Override
  public void insert(K key, V value) {
    delegate.insert(key, value);
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries) {
    delegate.update(entries);
  }

  @Override
  public void update(K key, V value) {
    delegate.update(key, value);
  }

  @Override
  public void delete(Stream<K> keys) {
    delegate.delete(keys);
  }

  @Override
  public void delete(K key) {
    delegate.delete(key);
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification) {
    return delegate.entries(specification);
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification) {
    return delegate.keys(specification);
  }

  @Override
  public Stream<V> values(Specification<K, V> specification) {
    return delegate.values(specification);
  }

  @Override
  public boolean exists(K key) {
    return delegate.exists(key);
  }

  @Override
  public Optional<V> get(K key) {
    return delegate.get(key);
  }

}
