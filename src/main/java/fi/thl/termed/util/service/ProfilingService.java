package fi.thl.termed.util.service;

import static fi.thl.termed.util.DurationUtils.prettyPrintMillis;
import static java.lang.System.currentTimeMillis;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfilingService<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  private final Logger log;
  private final long limitInMillis;

  private final Service<K, V> delegate;

  public ProfilingService(Service<K, V> delegate, String loggerName, int limitInMillis) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInMillis = limitInMillis;
  }

  public ProfilingService(Service<K, V> delegate, Class<?> loggerName, int limitInMillis) {
    this.delegate = delegate;
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInMillis = limitInMillis;
  }

  @Override
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    return profile(
        () -> delegate.save(values, mode, opts, user),
        "Saved value stream (user: %s)", user.getUsername());
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    return profile(
        () -> delegate.save(value, mode, opts, user),
        "Saved %s (user: %s)", value.identifier(), user.getUsername());
  }

  @Override
  public void delete(Stream<K> keys, WriteOptions opts, User user) {
    profile(() -> {
      delegate.delete(keys, opts, user);
      return null;
    }, "Deleted key stream (user: %s)", user.getUsername());
  }

  @Override
  public void delete(K key, WriteOptions opts, User user) {
    profile(() -> {
      delegate.delete(key, opts, user);
      return null;
    }, "Deleted %s (user: %s)", key, user.getUsername());
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return profileStream(
        () -> delegate.keys(query, user),
        "Found keys with %s (user: %s)", query, user.getUsername());
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return profileStream(
        () -> delegate.values(query, user),
        "Found values with %s (user: %s)", query, user.getUsername());
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    return profile(() -> delegate.count(spec, user),
        "Counted with %s (user: %s)", spec, user.getUsername());
  }

  @Override
  public boolean exists(K key, User user) {
    return profile(() -> delegate.exists(key, user),
        "Exists %s (user: %s)", key, user.getUsername());
  }

  @Override
  public Optional<V> get(K key, User user, Select... selects) {
    return profile(() -> delegate.get(key, user, selects),
        "Got %s (user: %s)", key, user.getUsername());
  }

  private <E> E profile(Supplier<E> supplier, String format, Object... args) {
    long start = currentTimeMillis();
    E result = supplier.get();
    logDuration(currentTimeMillis() - start, format, args);
    return result;
  }

  private <E> Stream<E> profileStream(Supplier<Stream<E>> supplier, String format, Object... args) {
    long start = System.currentTimeMillis();
    return supplier.get().onClose(() -> logDuration(currentTimeMillis() - start, format, args));
  }

  private void logDuration(long durationInMillis, String msgFormat, Object[] args) {
    if (durationInMillis >= limitInMillis) {
      log.debug("{} in {}", String.format(msgFormat, args), prettyPrintMillis(durationInMillis));
    } else if (log.isTraceEnabled()) {
      log.trace("{} in {}", String.format(msgFormat, args), prettyPrintMillis(durationInMillis));
    }
  }

}
