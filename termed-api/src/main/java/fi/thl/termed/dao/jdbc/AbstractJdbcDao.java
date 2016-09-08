package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.MapUtils;

/**
 * Base class to help with implementing a Dao.
 */
public abstract class AbstractJdbcDao<K extends Serializable, V> implements Dao<K, V> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected JdbcTemplate jdbcTemplate;

  protected RowMapper<K> keyMapper;
  protected RowMapper<V> valueMapper;
  protected RowMapper<Map.Entry<K, V>> entryMapper;

  public AbstractJdbcDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);

    this.keyMapper = buildKeyMapper();
    this.valueMapper = buildValueMapper();
    this.entryMapper = new MapEntryRowMapper<K, V>(keyMapper, valueMapper);
  }

  protected abstract RowMapper<K> buildKeyMapper();

  protected abstract RowMapper<V> buildValueMapper();

  @Override
  public void insert(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      insert(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void update(Map<K, V> map) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      update(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void delete() {
    delete(getKeys());
  }

  @Override
  public void delete(Iterable<K> keys) {
    for (K key : keys) {
      delete(key);
    }
  }

  @Override
  public Map<K, V> getMap() {
    return MapUtils.newLinkedHashMap(get(entryMapper));
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return MapUtils.newLinkedHashMap(get((SqlSpecification<K, V>) specification, entryMapper));
    } else {
      log.warn("Scanning through all entries as SqlSpecification is not provided.");
      return Maps.filterEntries(getMap(), specification);
    }
  }

  @Override
  public Map<K, V> getMap(Iterable<K> keys) {
    return MapUtils.newLinkedHashMap(get(keys, entryMapper));
  }

  @Override
  public List<K> getKeys() {
    return get(keyMapper);
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return get((SqlSpecification<K, V>) specification, keyMapper);
    } else {
      return Lists.newArrayList(getMap(specification).keySet());
    }
  }

  @Override
  public List<V> getValues() {
    return get(valueMapper);
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return get((SqlSpecification<K, V>) specification, valueMapper);
    } else {
      return Lists.newArrayList(getMap(specification).values());
    }
  }

  @Override
  public List<V> getValues(Iterable<K> keys) {
    return get(keys, valueMapper);
  }

  @Override
  public V get(K key) {
    return get(key, valueMapper);
  }

  protected abstract <E> List<E> get(RowMapper<E> mapper);

  protected abstract <E> List<E> get(SqlSpecification<K, V> specification, RowMapper<E> mapper);

  protected <E> List<E> get(Iterable<K> keys, RowMapper<E> mapper) {
    List<E> values = Lists.newArrayList();
    for (K key : keys) {
      values.add(get(key, mapper));
    }
    return values;
  }

  protected abstract <E> E get(K key, RowMapper<E> mapper);

}
