package fi.thl.termed.util.dao;

import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public class CachedSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 100_000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 100_000;

  private final SystemDao<K, V> delegate;

  private final LoadingCache<Specification<K, V>, ImmutableList<K>> specificationCache;
  private final LoadingCache<K, Optional<V>> keyValueCache;

  private CachedSystemDao(SystemDao<K, V> delegate) {
    this(delegate, DEFAULT_SPECIFICATION_CACHE_SIZE, DEFAULT_KEY_VALUE_CACHE_SIZE);
  }

  private CachedSystemDao(SystemDao<K, V> delegate, long specCacheSize, long keyValueCacheSize) {
    this.delegate = delegate;
    this.specificationCache = CacheBuilder.newBuilder()
        .maximumSize(specCacheSize)
        .build(CacheLoader.from(spec -> toImmutableListAndClose(delegate.keys(spec))));
    this.keyValueCache = CacheBuilder.newBuilder()
        .maximumSize(keyValueCacheSize)
        .build(CacheLoader.from(delegate::get));
  }

  public static <K extends Serializable, V> CachedSystemDao<K, V> cache(SystemDao<K, V> delegate) {
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
    delegate.delete(keys.peek(keyValueCache::invalidate));
    specificationCache.invalidateAll();
  }

  @Override
  public void delete(K key) {
    delegate.delete(key);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public Stream<Tuple2<K, V>> entries(Specification<K, V> specification) {
    return keys(specification).map(key -> Tuple.of(key, getOrThrowException(key)));
  }

  @Override
  public Stream<K> keys(Specification<K, V> specification) {
    return specificationCache.getUnchecked(specification).stream();
  }

  @Override
  public Stream<V> values(Specification<K, V> specification) {
    return keys(specification).map(this::getOrThrowException);
  }

  // for internal use when key is expected to exist
  private V getOrThrowException(K key) {
    return get(key).orElseThrow(() -> new CacheException("Cached value not found for: " + key));
  }

  @Override
  public Optional<V> get(K key) {
    return keyValueCache.getUnchecked(key);
  }

  @Override
  public boolean exists(K key) {
    return get(key).isPresent();
  }

}
