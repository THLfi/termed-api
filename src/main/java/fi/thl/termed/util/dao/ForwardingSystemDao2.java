package fi.thl.termed.util.dao;

import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public class ForwardingSystemDao2<K extends Serializable, V> implements SystemDao2<K, V> {

  private SystemDao2<K, V> delegate;

  public ForwardingSystemDao2(SystemDao2<K, V> delegate) {
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
  public Stream<Tuple2<K, V>> getEntries(Specification<K, V> specification) {
    return delegate.getEntries(specification);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification) {
    return delegate.getKeys(specification);
  }

  @Override
  public Stream<V> getValues(Specification<K, V> specification) {
    return delegate.getValues(specification);
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
