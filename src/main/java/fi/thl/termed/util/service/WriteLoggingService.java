package fi.thl.termed.util.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.StreamUtils.partitionedPeek;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteLoggingService<K extends Serializable, V extends Identifiable<K>>
    extends ForwardingService<K, V> {

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
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    Consumer<Stream<V>> valueListLogger = vs ->
        log.info("save {} (user: {})",
            vs.map(Identifiable::identifier).collect(toImmutableList()),
            user.getUsername());

    Stream<V> valuesWithLogging = log.isInfoEnabled()
        ? partitionedPeek(values, 1000, valueListLogger)
        : values;

    return super.save(valuesWithLogging, mode, opts, user);
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    if (log.isInfoEnabled()) {
      log.info("save {} (user: {})", value.identifier(), user.getUsername());
    }

    return super.save(value, mode, opts, user);
  }

  @Override
  public void delete(Stream<K> ids, WriteOptions opts, User user) {
    Consumer<Stream<K>> keyListLogger = ks ->
        log.info("delete {} (user: {})", ks.collect(toImmutableList()), user.getUsername());

    Stream<K> idsWithLogging = log.isInfoEnabled()
        ? partitionedPeek(ids, 1000, keyListLogger)
        : ids;

    super.delete(idsWithLogging, opts, user);
  }

  @Override
  public void delete(K key, WriteOptions opts, User user) {
    if (log.isInfoEnabled()) {
      log.info("delete {} (user: {})", key, user.getUsername());
    }

    super.delete(key, opts, user);
  }

}
