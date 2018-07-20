package fi.thl.termed.util.dao;

import static fi.thl.termed.util.collect.StreamUtils.forEachAndClose;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class CachedSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 100_000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 100_000;

  private Cache<Specification<K, V>, List<K>> specificationCache;
  private Cache<K, Optional<V>> keyValueCache;

  private SystemDao<K, V> delegate;

  private CachedSystemDao(SystemDao<K, V> delegate) {
    this(delegate, DEFAULT_SPECIFICATION_CACHE_SIZE, DEFAULT_KEY_VALUE_CACHE_SIZE);
  }

  private CachedSystemDao(SystemDao<K, V> delegate, long specCacheSize, long keyValueCacheSize) {
    this.delegate = delegate;
    this.specificationCache = CacheBuilder.newBuilder()
        .maximumSize(specCacheSize).recordStats().build();
    this.keyValueCache = CacheBuilder.newBuilder()
        .maximumSize(keyValueCacheSize).recordStats().build();
  }

  public static <K extends Serializable, V> CachedSystemDao<K, V> cache(
      SystemDao<K, V> delegate) {
    return new CachedSystemDao<>(delegate);
  }

  @Subscribe
  public void clearCachesOn(InvalidateCachesEvent e) {
    specificationCache.invalidateAll();
    keyValueCache.invalidateAll();
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    delegate.insert(entries.peek(e -> keyValueCache.invalidate(e._1)));
    specificationCache.invalidateAll();
  }

  @Override
  public void insert(K key, V value) {
    delegate.insert(key, value);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries) {
    delegate.update(entries.peek(e -> keyValueCache.invalidate(e._1)));
    specificationCache.invalidateAll();
  }

  @Override
  public void update(K key, V value) {
    delegate.update(key, value);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public void delete(Stream<K> keys) {
    delegate.delete(keys.peek(k -> keyValueCache.invalidate(k)));
    specificationCache.invalidateAll();
  }

  @Override
  public void delete(K key) {
    delegate.delete(key);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public Stream<Tuple2<K, V>> getEntries(Specification<K, V> specification) {
    Stream.Builder<Tuple2<K, V>> builder = Stream.builder();
    forEachAndClose(getKeys(specification),
        key -> builder.accept(Tuple.of(key, get(key).orElseThrow(IllegalStateException::new))));
    return builder.build();
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification) {
    try {
      return specificationCache
          .get(specification, () -> toListAndClose(delegate.getKeys(specification)))
          .stream();
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    }
  }

  @Override
  public Stream<V> getValues(Specification<K, V> specification) {
    return getKeys(specification).map(key -> {
      Optional<V> value = get(key);

      if (!value.isPresent()) {
        throw new IllegalStateException();
      }

      return value.get();
    });
  }

  @Override
  public Optional<V> get(K key) {
    try {
      return keyValueCache.get(key, () -> delegate.get(key));
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    }
  }

  @Override
  public boolean exists(K key) {
    return get(key).isPresent() || delegate.exists(key);
  }

}
