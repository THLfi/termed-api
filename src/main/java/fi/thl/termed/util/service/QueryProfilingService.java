package fi.thl.termed.util.service;

import static fi.thl.termed.util.DurationUtils.prettyPrint;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryProfilingService<K extends Serializable, V> extends ForwardingService<K, V> {

  private Logger log;
  private long limitInNanos;

  public QueryProfilingService(Service<K, V> delegate, String loggerName, int limitInMillis) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInNanos = MILLISECONDS.toNanos(limitInMillis);
  }

  public QueryProfilingService(Service<K, V> delegate, Class<?> loggerName, int limitInMillis) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInNanos = MILLISECONDS.toNanos(limitInMillis);
  }

  @Override
  public List<V> getValues(User user) {
    return profile(() -> super.getValues(user), "getValues (%s)", user.getUsername());
  }

  @Override
  public List<V> getValues(Specification<K, V> spec, User user) {
    return profile(() -> super.getValues(spec, user), "getValues %s (%s)", spec,
        user.getUsername());
  }

  @Override
  public List<V> getValues(Query<K, V> query, User user) {
    return profile(() -> super.getValues(query, user), "getValues %s (%s)", query,
        user.getUsername());
  }

  @Override
  public Stream<V> getValueStream(User user) {
    return profileStream(() -> super.getValueStream(user), "getValueStream (%s)",
        user.getUsername());
  }

  @Override
  public Stream<V> getValueStream(Specification<K, V> spec, User user) {
    return profileStream(() -> super.getValueStream(spec, user), "getValueStream %s (%s)", spec,
        user.getUsername());
  }

  @Override
  public Stream<V> getValueStream(Query<K, V> query, User user) {
    return profileStream(() -> super.getValueStream(query, user), "getValueStream %s (%s)", query,
        user.getUsername());
  }

  @Override
  public List<K> getKeys(User user) {
    return profile(() -> super.getKeys(user), "getKeys (%s)", user.getUsername());
  }

  @Override
  public List<K> getKeys(Specification<K, V> spec, User user) {
    return profile(() -> super.getKeys(spec, user), "getKeys %s (%s)" + spec,
        user.getUsername());
  }

  @Override
  public List<K> getKeys(Query<K, V> query, User user) {
    return profile(() -> super.getKeys(query, user), "getKeys %s (%s)", query,
        user.getUsername());
  }

  @Override
  public Stream<K> getKeyStream(User user) {
    return profileStream(() -> super.getKeyStream(user), "getKeyStream (%s)", user.getUsername());
  }

  @Override
  public Stream<K> getKeyStream(Specification<K, V> spec, User user) {
    return profileStream(() -> super.getKeyStream(spec, user), "getKeyStream %s (%s)" + spec,
        user.getUsername());
  }

  @Override
  public Stream<K> getKeyStream(Query<K, V> query, User user) {
    return profileStream(() -> super.getKeyStream(query, user), "getKeyStream %s (%s)", query,
        user.getUsername());
  }

  private <E> E profile(Supplier<E> supplier, String format, Object... args) {
    long start = System.nanoTime();
    E result = supplier.get();
    logDuration(System.nanoTime() - start, format, args);
    return result;
  }

  private <E> Stream<E> profileStream(Supplier<Stream<E>> supplier, String format, Object... args) {
    long start = System.nanoTime();
    return supplier.get().onClose(() -> logDuration(System.nanoTime() - start, format, args));
  }

  private void logDuration(long durationInNanos, String msgFormat, Object[] args) {
    if (durationInNanos >= limitInNanos) {
      log.info("{} in {}", String.format(msgFormat, args), prettyPrint(durationInNanos));
    }
  }

}
