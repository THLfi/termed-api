package fi.thl.termed.util.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Results;

public class LoggingService<K extends Serializable, V> implements Service<K, V> {

  private Service<K, V> delegate;
  private Logger log;

  public LoggingService(Service<K, V> delegate, String loggerName) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
  }

  public LoggingService(Service<K, V> delegate, Class<?> loggerName) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
  }

  @Override
  public List<K> save(List<V> values, User user) {
    log.info("save {} values (user: {})", values.size(), user.getUsername());
    return delegate.save(values, user);
  }

  @Override
  public K save(V value, User user) {
    log.info("save {} (user: {})", value, user.getUsername());
    return delegate.save(value, user);
  }

  @Override
  public void delete(K id, User user) {
    log.info("delete {} (user: {})", id, user.getUsername());
    delegate.delete(id, user);
  }

  @Override
  public List<V> get(List<K> ids, User user) {
    log.info("get {} (user: {})", ids.size(), user.getUsername());
    return delegate.get(ids, user);
  }

  @Override
  public Results<V> get(Query<K, V> query, User user) {
    log.info("get {} (user: {})", query, user.getUsername());
    return delegate.get(query, user);
  }

  @Override
  public Results<K> getKeys(Query<K, V> query, User user) {
    log.info("getKeys {} (user: {})", query, user.getUsername());
    return delegate.getKeys(query, user);
  }

  @Override
  public Optional<V> get(K id, User user) {
    log.info("get {} (user: {})", id, user.getUsername());
    return delegate.get(id, user);
  }

}
