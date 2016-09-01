package fi.thl.termed.service.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;

public class LoggingService<K extends Serializable, V> extends ForwardingService<K, V> {

  private Logger log = LoggerFactory.getLogger(getClass());
  private Class<V> valueClass;

  public LoggingService(Service<K, V> delegate, Class<V> valueClass) {
    super(delegate);
    this.valueClass = valueClass;
  }

  @Override
  public void save(List<V> values, User currentUser) {
    log.info("{} save {} values (user: {})", valueClass.getSimpleName(), values.size(),
             currentUser.getUsername());
    super.save(values, currentUser);
  }

  @Override
  public void save(V value, User currentUser) {
    log.info("{} save {} (user: {})", valueClass.getSimpleName(), value, currentUser.getUsername());
    super.save(value, currentUser);
  }

  @Override
  public void delete(K id, User currentUser) {
    log.info("{} delete {} (user: {})", valueClass.getSimpleName(), id, currentUser.getUsername());
    super.delete(id, currentUser);
  }

  @Override
  public List<V> get(User currentUser) {
    log.info("{} get (user: {})", valueClass.getSimpleName(), currentUser.getUsername());
    return super.get(currentUser);
  }

  @Override
  public List<V> get(Specification<K, V> specification, User currentUser) {
    log.info("{} get {} (user: {})", valueClass.getSimpleName(), specification,
             currentUser.getUsername());
    return super.get(specification, currentUser);
  }

  @Override
  public List<V> get(Query query, User currentUser) {
    log.info("{} get {} (user: {})", valueClass.getSimpleName(), query, currentUser.getUsername());
    return super.get(query, currentUser);
  }

  @Override
  public V get(K id, User currentUser) {
    log.info("{} get {} (user: {})", valueClass.getSimpleName(), id, currentUser.getUsername());
    return super.get(id, currentUser);
  }

}
