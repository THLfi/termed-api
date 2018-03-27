package fi.thl.termed.util.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fi.thl.termed.domain.event.InvalidateCachesEvent;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CachedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

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

  public static <K extends Serializable, V> CachedSystemDao<K, V> cache(SystemDao<K, V> delegate) {
    return new CachedSystemDao<>(delegate);
  }

  @Subscribe
  public void clearCachesOn(InvalidateCachesEvent e) {
    specificationCache.invalidateAll();
    keyValueCache.invalidateAll();
  }

  @Override
  public void insert(Map<K, V> map) {
    delegate.insert(map);
    keyValueCache.invalidateAll(map.keySet());
    specificationCache.invalidateAll();
  }

  @Override
  public void insert(K key, V value) {
    delegate.insert(key, value);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public void update(Map<K, V> map) {
    delegate.update(map);
    keyValueCache.invalidateAll(map.keySet());
    specificationCache.invalidateAll();
  }

  @Override
  public void update(K key, V value) {
    delegate.update(key, value);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public void delete(List<K> keys) {
    delegate.delete(keys);
    keyValueCache.invalidateAll(keys);
    specificationCache.invalidateAll();
  }

  @Override
  public void delete(K key) {
    delegate.delete(key);
    keyValueCache.invalidate(key);
    specificationCache.invalidateAll();
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    Map<K, V> results = new LinkedHashMap<>();
    getKeys(specification).forEach(key -> get(key).ifPresent(value -> results.put(key, value)));
    return results;
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    try {
      return specificationCache.get(specification, () -> delegate.getKeys(specification));
    } catch (ExecutionException e) {
      throw new UncheckedExecutionException(e);
    }
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    List<V> values = new ArrayList<>();
    getKeys(specification).forEach(key -> get(key).ifPresent(values::add));
    return values;
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
