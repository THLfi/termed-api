package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import fi.thl.termed.util.collect.MapUtils;

public class MapEntryRowMapper<K, V> implements RowMapper<Map.Entry<K, V>> {

  private RowMapper<K> keyMapper;
  private RowMapper<V> valueMapper;

  public MapEntryRowMapper(RowMapper<K> keyMapper, RowMapper<V> valueMapper) {
    this.keyMapper = keyMapper;
    this.valueMapper = valueMapper;
  }

  @Override
  public Map.Entry<K, V> mapRow(ResultSet rs, int rowNum) throws SQLException {
    return MapUtils.simpleEntry(keyMapper.mapRow(rs, rowNum), valueMapper.mapRow(rs, rowNum));
  }

}
