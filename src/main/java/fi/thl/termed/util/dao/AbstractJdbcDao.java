package fi.thl.termed.util.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import fi.thl.termed.util.ProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.SqlSpecification;

/**
 * Base class to help with implementing a JDBC Dao.
 */
public abstract class AbstractJdbcDao<K extends Serializable, V> implements SystemDao<K, V> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected JdbcTemplate jdbcTemplate;

  private RowMapper<K> keyMapper;
  private RowMapper<V> valueMapper;
  private RowMapper<Map.Entry<K, V>> entryMapper;

  public AbstractJdbcDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);

    this.keyMapper = buildKeyMapper();
    this.valueMapper = buildValueMapper();
    this.entryMapper = (rs, rowNum) ->
        new SimpleEntry<>(keyMapper.mapRow(rs, rowNum), valueMapper.mapRow(rs, rowNum));
  }

  protected abstract RowMapper<K> buildKeyMapper();

  protected abstract RowMapper<V> buildValueMapper();

  @Override
  public void insert(Map<K, V> map) {
    ProgressReporter reporter = new ProgressReporter(log, "Inserted", 1000, map.size());

    map.forEach((k, v) -> {
      insert(k, v);
      reporter.tick();
    });

    if (map.size() >= 1000) {
      reporter.report();
    }
  }

  @Override
  public void update(Map<K, V> map) {
    map.forEach(this::update);
  }

  @Override
  public void delete(List<K> keys) {
    keys.forEach(this::delete);
  }

  @Override
  public Map<K, V> getMap() {
    return ImmutableMap.copyOf(get(entryMapper));
  }

  @Override
  public Map<K, V> getMap(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return ImmutableMap.copyOf(get((SqlSpecification<K, V>) specification, entryMapper));
    } else {
      log.warn("Scanning through all entries as SqlSpecification is not provided.");
      return Maps.filterEntries(getMap(), e -> specification.test(e.getKey(), e.getValue()));
    }
  }

  @Override
  public Map<K, V> getMap(List<K> keys) {
    return ImmutableMap.copyOf(get(keys, entryMapper));
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
      return new ArrayList<>(getMap(specification).keySet());
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
      return new ArrayList<>(getMap(specification).values());
    }
  }

  @Override
  public List<V> getValues(List<K> keys) {
    return get(keys, valueMapper);
  }

  @Override
  public Optional<V> get(K key) {
    return get(key, valueMapper);
  }

  protected abstract <E> List<E> get(RowMapper<E> mapper);

  protected abstract <E> List<E> get(SqlSpecification<K, V> specification, RowMapper<E> mapper);

  protected <E> List<E> get(Iterable<K> keys, RowMapper<E> mapper) {
    List<E> values = new ArrayList<>();
    for (K key : keys) {
      get(key, mapper).ifPresent(values::add);
    }
    return values;
  }

  protected abstract <E> Optional<E> get(K key, RowMapper<E> mapper);

}
