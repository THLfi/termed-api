package fi.thl.termed.util.dao;

import com.google.common.base.Ascii;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fi.thl.termed.domain.event.ClearDaoCachesEvent;
import fi.thl.termed.domain.event.LogDiagnosticsEvent;
import fi.thl.termed.util.specification.Specification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedSystemDao<K extends Serializable, V> extends AbstractSystemDao<K, V> {

  private static final int DEFAULT_SPECIFICATION_CACHE_SIZE = 1000;
  private static final int DEFAULT_KEY_VALUE_CACHE_SIZE = 1000;

  private Logger log = LoggerFactory.getLogger(getClass());
  private String name;

  private Cache<Specification<K, V>, List<K>> specificationCache;
  private Cache<K, Optional<V>> keyValueCache;

  private SystemDao<K, V> delegate;

  public CachedSystemDao(SystemDao<K, V> delegate) {
    this(delegate, DEFAULT_SPECIFICATION_CACHE_SIZE, DEFAULT_KEY_VALUE_CACHE_SIZE, null);
  }

  public CachedSystemDao(SystemDao<K, V> delegate, String name) {
    this(delegate, DEFAULT_SPECIFICATION_CACHE_SIZE, DEFAULT_KEY_VALUE_CACHE_SIZE, name);
  }

  public CachedSystemDao(SystemDao<K, V> delegate, long specCacheSize, long keyValueCacheSize,
      String name) {
    this.delegate = delegate;
    this.specificationCache = CacheBuilder.newBuilder()
        .maximumSize(specCacheSize).recordStats().build();
    this.keyValueCache = CacheBuilder.newBuilder()
        .maximumSize(keyValueCacheSize).recordStats().build();
    this.name = Ascii.truncate(Strings.nullToEmpty(name), 5, "");
  }

  @Subscribe
  public void clearCachesOn(ClearDaoCachesEvent e) {
    specificationCache.invalidateAll();
    keyValueCache.invalidateAll();
  }

  @Subscribe
  public void logDiagnosticsOn(LogDiagnosticsEvent e) {
    log.info("{} specif {}", name, specificationCache.stats());
    log.info("{} values {}", name, keyValueCache.stats());
  }

  @Override
  public void insert(Map<K, V> map) {
    specificationCache.invalidateAll();
    keyValueCache.invalidateAll(map.keySet());
    delegate.insert(map);
  }

  @Override
  public void insert(K key, V value) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(key);
    delegate.insert(key, value);
  }

  @Override
  public void update(Map<K, V> map) {
    specificationCache.invalidateAll();
    keyValueCache.invalidateAll(map.keySet());
    delegate.update(map);
  }

  @Override
  public void update(K key, V value) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(key);
    delegate.update(key, value);
  }

  @Override
  public void delete(List<K> keys) {
    specificationCache.invalidateAll();
    keyValueCache.invalidate(keys);
    delegate.delete(keys);
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
