package fi.thl.termed.util.dao;

import static fi.thl.termed.util.collect.StreamUtils.forEachAndClose;

import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.SqlSpecification;
import fi.thl.termed.util.spring.jdbc.StreamingJdbcTemplate;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Base class to help with implementing a JDBC Dao.
 */
public abstract class AbstractJdbcDao2<K extends Serializable, V> implements SystemDao2<K, V> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected StreamingJdbcTemplate jdbcTemplate;

  private RowMapper<K> keyMapper;
  private RowMapper<V> valueMapper;
  private RowMapper<Tuple2<K, V>> entryMapper;

  public AbstractJdbcDao2(DataSource dataSource) {
    this.jdbcTemplate = new StreamingJdbcTemplate(dataSource);

    this.keyMapper = buildKeyMapper();
    this.valueMapper = buildValueMapper();
    this.entryMapper = (rs, rowNum) -> Tuple.of(
        keyMapper.mapRow(rs, rowNum), valueMapper.mapRow(rs, rowNum));
  }

  protected abstract RowMapper<K> buildKeyMapper();

  protected abstract RowMapper<V> buildValueMapper();

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    forEachAndClose(entries, e -> insert(e._1, e._2));
  }

  @Override
  public void update(Stream<Tuple2<K, V>> entries) {
    forEachAndClose(entries, e -> update(e._1, e._2));
  }

  @Override
  public void delete(Stream<K> keys) {
    forEachAndClose(keys, this::delete);
  }

  @Override
  public Stream<Tuple2<K, V>> getEntries(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return get((SqlSpecification<K, V>) specification, entryMapper);
    } else {
      log.warn("Scanning through all entries as SqlSpecification is not provided.");
      return getEntries(new MatchAll<>()).filter(e -> specification.test(e._1, e._2));
    }
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return get((SqlSpecification<K, V>) specification, keyMapper);
    } else {
      return getEntries(specification).map(e -> e._1);
    }
  }

  @Override
  public Stream<V> getValues(Specification<K, V> specification) {
    if (specification instanceof SqlSpecification) {
      return get((SqlSpecification<K, V>) specification, valueMapper);
    } else {
      return getEntries(specification).map(e -> e._2);
    }
  }

  @Override
  public Optional<V> get(K key) {
    return get(key, valueMapper);
  }

  protected abstract <E> Stream<E> get(SqlSpecification<K, V> specification, RowMapper<E> mapper);

  protected abstract <E> Optional<E> get(K key, RowMapper<E> mapper);

}
