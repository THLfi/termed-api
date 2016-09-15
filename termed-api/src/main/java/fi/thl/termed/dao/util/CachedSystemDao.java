package fi.thl.termed.dao.util;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.AbstractSystemDao;
import fi.thl.termed.dao.SystemDao;
import fi.thl.termed.spesification.Specification;

public class CachedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 1000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 10000;

  private Logger log = LoggerFactory.getLogger(getClass());

  private LoadingCache<Specification<K, V>, List<K>> specificationCache;
  // cached value is wrapped in optional as CacheLoader can't return null values but Dao can
  private LoadingCache<K, Optional<V>> keyValueCache;

  private SystemDao<K, V> delegate;

  public CachedSystemDao(SystemDao<K, V> delegate) {
    this.delegate = delegate;
    this.specificationCache = CacheBuilder.newBuilder()
        .maximumSize(DEFAULT_SPECIFICATION_CACHE_SIZE)
        .build(new SpecificationCacheLoader());
    this.keyValueCache = CacheBuilder.newBuilder()
        .maximumSize(DEFAULT_KEY_VALUE_CACHE_SIZE)
        .build(new KeyValueCacheLoader());
  }

  public static <K extends Serializable, V> CachedSystemDao<K, V> create(SystemDao<K, V> delegate) {
    return new CachedSystemDao<K, V>(delegate);
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
    Map<K, V> results = Maps.newLinkedHashMap();
    for (K key : getKeys(specification)) {
      results.put(key, get(key));
    }
    return results;
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    return specificationCache.getUnchecked(specification);
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    List<V> values = Lists.newArrayList();
    for (K key : getKeys(specification)) {
      values.add(get(key));
    }
    return values;
  }

  @Override
  public V get(K key) {
    return keyValueCache.getUnchecked(key).orNull();
  }

  private class SpecificationCacheLoader extends CacheLoader<Specification<K, V>, List<K>> {

    public List<K> load(Specification<K, V> specification) {
      return delegate.getKeys(specification);
    }
  }

  private class KeyValueCacheLoader extends CacheLoader<K, Optional<V>> {

    public Optional<V> load(K key) {
      return Optional.fromNullable(delegate.get(key));
    }
  }

}
