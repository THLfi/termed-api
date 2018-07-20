package fi.thl.termed.util.service;

import static fi.thl.termed.util.DurationUtils.prettyPrint;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Query;
import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryProfilingService<K extends Serializable, V> extends ForwardingService2<K, V> {

  private Logger log;
  private long limitInNanos;

  public QueryProfilingService(Service2<K, V> delegate, String loggerName, int limitInMillis) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInNanos = MILLISECONDS.toNanos(limitInMillis);
  }

  public QueryProfilingService(Service2<K, V> delegate, Class<?> loggerName, int limitInMillis) {
    super(delegate);
    this.log = LoggerFactory.getLogger(loggerName);
    this.limitInNanos = MILLISECONDS.toNanos(limitInMillis);
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return profile(() -> super.values(query, user), "values %s (%s)", query, user.getUsername());
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return profileStream(() -> super.keys(query, user), "keys %s (%s)", query, user.getUsername());
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
      log.debug("{} in {}", String.format(msgFormat, args), prettyPrint(durationInNanos));
    } else if (log.isTraceEnabled()) {
      log.trace("{} in {}", String.format(msgFormat, args), prettyPrint(durationInNanos));
    }
  }

}
