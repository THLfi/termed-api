package fi.thl.termed.util.service;

import static com.google.common.base.Ascii.truncate;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteLoggingService<K extends Serializable, V> extends ForwardingService<K, V> {

  private Logger log;

  public WriteLoggingService(Service<K, V> delegate, String loggerName) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
  }

  public WriteLoggingService(Service<K, V> delegate, Class<?> loggerName) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
  }

  @Override
  public List<K> save(List<V> values, SaveMode mode, WriteOptions opts, User user) {
    if (log.isTraceEnabled()) {
      log.trace("save {} (user: {})", values, user.getUsername());
    } else if (log.isDebugEnabled()) {
      log.debug("save {} values (user: {})", values.size(), user.getUsername());
    }
    return super.save(values, mode, opts, user);
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    if (log.isTraceEnabled()) {
      log.trace("save {} (user: {})", value, user.getUsername());
    } else if (log.isDebugEnabled()) {
      log.debug("save {} (user: {})", truncate(value.toString(), 150, "..."), user.getUsername());
    }
    return super.save(value, mode, opts, user);
  }

  @Override
  public void delete(List<K> ids, WriteOptions opts, User user) {
    if (log.isTraceEnabled()) {
      log.trace("delete {} (user: {})", ids, user.getUsername());
    } else if (log.isDebugEnabled()) {
      log.debug("delete {} values (user: {})", ids.size(), user.getUsername());
    }
    super.delete(ids, opts, user);
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    if (log.isDebugEnabled()) {
      log.debug("delete {} (user: {})", id, user.getUsername());
    }
    super.delete(id, opts, user);
  }

  @Override
  public List<K> saveAndDelete(List<V> saves, List<K> deletes, SaveMode mode, WriteOptions opts,
      User user) {
    if (log.isTraceEnabled()) {
      log.trace("save {} and delete {} (user: {})", saves, deletes, user.getUsername());
    } else if (log.isDebugEnabled()) {
      log.debug("save {} values and delete {} values (user: {})",
          saves.size(), deletes.size(), user.getUsername());
    }
    return super.saveAndDelete(saves, deletes, mode, opts, user);
  }

}
