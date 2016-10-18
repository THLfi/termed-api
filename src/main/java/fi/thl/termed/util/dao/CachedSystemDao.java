package fi.thl.termed.util.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import fi.thl.termed.util.specification.Specification;

public class CachedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 1000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 10000;

  private Cache<Specification<K, V>, List<K>> specificationCache;
  private Cache<K, Optional<V>> keyValueCache;

  private SystemDao<K, V> delegate;

  public CachedSystemDao(SystemDao<K, V> delegate) {
    this.delegate = delegate;
    this.specificationCache = CacheBuilder.newBuilder()
        .maximumSize(DEFAULT_SPECIFICATION_CACHE_SIZE).build();
    this.keyValueCache = CacheBuilder.newBuilder()
        .maximumSize(DEFAULT_KEY_VALUE_CACHE_SIZE).build();
  }

  @Override
  public void insert(K key, V value) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(key);
    delegate.insert(key, value);
  }

  @Override
  public void update(K key, V value) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(key);
    delegate.update(key, value);
  }

  @Override
  public void delete(K key) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(key);
    delegate.delete(key);
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    Map<K, V> results = new LinkedHashMap<>();
    getKeys(specification).forEach(
        key -> get(key).ifPresent(value -> results.put(key, value)));
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
