package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteLoggingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;
  private Logger log;

  public WriteLoggingService(Service<K, V> delegate, String loggerName) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
  }

  public WriteLoggingService(Service<K, V> delegate, Class<?> loggerName) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
  }

  @Override
  public List<K> save(List<V> values, User user, Arg... args) {
    log.info("save {} values (user: {})", values.size(), user.getUsername());
    return delegate.save(values, user, args);
  }

  @Override
  public K save(V value, User user, Arg... args) {
    log.info("save {} (user: {})", value, user.getUsername());
    return delegate.save(value, user, args);
  }

  @Override
  public void delete(List<K> ids, User user, Arg... args) {
    log.info("delete {} (user: {})", ids, user.getUsername());
    delegate.delete(ids, user, args);
  }

  @Override
  public void delete(K id, User user, Arg... args) {
    log.info("delete {} (user: {})", id, user.getUsername());
    delegate.delete(id, user, args);
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, User user, Arg... args) {
    log.info("delete {} and save {} values (user: {})", deletes, saves.size(), user.getUsername());
    return delegate.deleteAndSave(deletes, saves, user, args);
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, User user, Arg... args) {
    return delegate.get(specification, user, args);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, User user, Arg... args) {
    return delegate.getKeys(specification, user, args);
  }

  @Override
  public long count(Specification<K, V> specification, User user, Arg... args) {
    return delegate.count(specification, user, args);
  }

  @Override
  public boolean exists(K key, User user, Arg... args) {
    return delegate.exists(key, user, args);
  }

  @Override
  public Stream<V> get(List<K> ids, User user, Arg... args) {
    return delegate.get(ids, user, args);
  }

  @Override
  public Optional<V> get(K id, User user, Arg... args) {
    return delegate.get(id, user, args);
  }

}
