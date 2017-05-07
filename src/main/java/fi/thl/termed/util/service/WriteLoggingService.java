package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
  public List<K> save(List<V> values, Map<String, Object> args, User user) {
    log.info("save {} values (user: {})", values.size(), user.getUsername());
    return delegate.save(values, args, user);
  }

  @Override
  public K save(V value, Map<String, Object> args, User user) {
    log.info("save {} (user: {})", value, user.getUsername());
    return delegate.save(value, args, user);
  }

  @Override
  public void delete(List<K> ids, Map<String, Object> args, User user) {
    log.info("delete {} (user: {})", ids, user.getUsername());
    delegate.delete(ids, args, user);
  }

  @Override
  public void delete(K id, Map<String, Object> args, User user) {
    log.info("delete {} (user: {})", id, user.getUsername());
    delegate.delete(id, args, user);
  }

  @Override
  public List<K> deleteAndSave(List<K> deletes, List<V> saves, Map<String, Object> args,
      User user) {
    log.info("delete {} (user: {})", deletes, user.getUsername());
    log.info("save {} values (user: {})", saves.size(), user.getUsername());
    return delegate.deleteAndSave(deletes, saves, args, user);
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, Map<String, Object> args, User user) {
    return delegate.get(specification, args, user);
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, Map<String, Object> args, User user) {
    return delegate.getKeys(specification, args, user);
  }

  @Override
  public Stream<V> get(List<K> ids, Map<String, Object> args, User user) {
    return delegate.get(ids, args, user);
  }

  @Override
  public Optional<V> get(K id, Map<String, Object> args, User user) {
    return delegate.get(id, args, user);
  }

}
