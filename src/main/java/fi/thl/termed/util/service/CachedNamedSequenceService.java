package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CachedNamedSequenceService<K extends Serializable> implements NamedSequenceService<K> {

  private static final long DEFAULT_MIN_INCREMENT = 1000L;

  private final NamedSequenceService<K> delegate;
  private final long minIncrement;
  private final Map<K, Long> cache = new HashMap<>();

  public CachedNamedSequenceService(NamedSequenceService<K> delegate) {
    this(delegate, DEFAULT_MIN_INCREMENT);
  }

  public CachedNamedSequenceService(NamedSequenceService<K> delegate, long minIncrement) {
    this.delegate = delegate;
    this.minIncrement = minIncrement;
  }

  @Override
  public Long get(K sequenceId, User user) {
    Long actual = delegate.get(sequenceId, user);
    return cache.computeIfAbsent(sequenceId, (seq) -> actual);
  }

  @Override
  public Long getAndAdvance(K sequenceId, User user) {
    return getAndAdvance(sequenceId, 1L, user);
  }

  @Override
  public Long getAndAdvance(K sequenceId, Long increment, User user) {
    long actual = delegate.get(sequenceId, user);
    long cached = cache.computeIfAbsent(sequenceId, (seq) -> actual);

    long next = cached + increment;

    if (next > actual) {
      delegate.getAndAdvance(sequenceId, Math.max(increment, minIncrement), user);
    }

    cache.put(sequenceId, next);

    return cached;
  }

  @Override
  public void set(K sequenceId, Long value, User user) {
    cache.remove(sequenceId);
    delegate.set(sequenceId, value, user);
  }

  @Override
  public void close() {
    User cacheDestructor = User.newAdmin("cache-destructor");
    // backtrack delegate to actual latest value
    cache.forEach((key, value) -> delegate.set(key, value, cacheDestructor));
  }

}
