package fi.thl.termed.util.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.SpecificationQuery;

public class LoggingService<K extends Serializable, V> extends ForwardingService<K, V> {

  private Logger log = LoggerFactory.getLogger(getClass());
  private Class<V> valueClass;

  public LoggingService(Service<K, V> delegate, Class<V> valueClass) {
    super(delegate);
    this.valueClass = valueClass;
  }

  @Override
  public List<K> save(List<V> values, User user) {
    log.info("{} save {} values (user: {})", valueClass.getSimpleName(), values.size(),
             user.getUsername());
    return super.save(values, user);
  }

  @Override
  public K save(V value, User user) {
    log.info("{} save {} (user: {})", valueClass.getSimpleName(), value, user.getUsername());
    return super.save(value, user);
  }

  @Override
  public void delete(K id, User user) {
    log.info("{} delete {} (user: {})", valueClass.getSimpleName(), id, user.getUsername());
    super.delete(id, user);
  }

  @Override
  public List<V> get(SpecificationQuery<K, V> specification, User user) {
    log.info("{} get {} (user: {})", valueClass.getSimpleName(), specification, user.getUsername());
    return super.get(specification, user);
  }

  @Override
  public java.util.Optional<V> get(K id, User user) {
    log.info("{} get {} (user: {})", valueClass.getSimpleName(), id, user.getUsername());
    return super.get(id, user);
  }

}
