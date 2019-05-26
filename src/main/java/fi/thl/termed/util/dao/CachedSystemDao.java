package fi.thl.termed.util.dao;

import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class CachedSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 100_000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 100_000;

  private final SystemDao<K, V> delegate;

  private final Cache<Specification<K, V>, ImmutableList<K>> specificationCache;
  private final Cache<K, Optional<V>> keyValueCache;

  private final Lock readLock;
  private final Lock writeLock;

  private CachedSystemDao(SystemDao<K, V> delegate) {
    this(delegate, DEFAULT_SPECIFICATION_CACHE_SIZE, DEFAULT_KEY_VALUE_CACHE_SIZE);
  }

  private CachedSystemDao(SystemDao<K, V> delegate, long specCacheSize, long keyValueCacheSize) {
    this.delegate = delegate;

    this.specificationCache = CacheBuilder.newBuilder().maximumSize(specCacheSize).build();
    this.keyValueCache = CacheBuilder.newBuilder().maximumSize(keyValueCacheSize).build();

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    this.readLock = readWriteLock.readLock();
    this.writeLock = readWriteLock.writeLock();
  }

  public static <K extends Serializable, V> CachedSystemDao<K, V> cache(SystemDao<K, V> delegate) {
    return new CachedSystemDao<>(delegate);
  }

  @Subscribe
  public void clearCachesOn(InvalidateCachesEvent e) {
    writeLock.lock();
    try {
      specificationCache.invalidateAll();
      keyValueCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    writeLock.lock();
    try {
      delegate.insert(entries.peek(e -> keyValueCache.invalidate(e._1)));
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void insert(K key, V value) {
    writeLock.lock();
    try {
      delegate.insert(key, value);
      keyValueCache.invalidate(key);
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries) {
    writeLock.lock();
    try {
      delegate.update(entries.peek(e -> keyValueCache.invalidate(e._1)));
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void update(K key, V value) {
    writeLock.lock();
    try {
      delegate.update(key, value);
      keyValueCache.invalidate(key);
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void delete(Stream<K> keys) {
    writeLock.lock();
    try {
      delegate.delete(keys.peek(keyValueCache::invalidate));
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void delete(K key) {
    writeLock.lock();
    try {
      delegate.delete(key);
      keyValueCache.invalidate(key);
      specificationCache.invalidateAll();
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification) {
    readLock.lock();
    try {
      return keys(specification)
          .map(key -> Tuple.of(key, get(key).orElseThrow(IllegalStateException::new)));
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification) {
    readLock.lock();
    try {
      return specificationCache
          .get(specification, () -> toImmutableListAndClose(delegate.keys(specification)))
          .stream();
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Stream<V> values(Specification<K, V> specification) {
    readLock.lock();
    try {
      return keys(specification)
          .map(key -> get(key).orElseThrow(IllegalStateException::new));
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public Optional<V> get(K key) {
    readLock.lock();
    try {
      return keyValueCache.get(key, () -> delegate.get(key));
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean exists(K key) {
    readLock.lock();
    try {
      return get(key).isPresent();
    } finally {
      readLock.unlock();
    }
  }

}
