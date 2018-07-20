package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeSequenceDao extends AbstractJdbcDao2<TypeId, Long> {

  public JdbcNodeSequenceDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TypeId typeId, Long value) {
    jdbcTemplate.update(
        "insert into node_sequence (graph_id, type_id, value) values (?, ?, ?)",
        typeId.getGraphId(),
        typeId.getId(),
        value);
  }

  @Override
  public void update(TypeId typeId, Long value) {
    jdbcTemplate.update(
        "update node_sequence set value = ? where graph_id = ? and type_id = ?",
        value,
        typeId.getGraphId(),
        typeId.getId());
  }

  @Override
  public void delete(TypeId typeId) {
    jdbcTemplate.update(
        "delete from node_sequence where graph_id = ? and type_id = ?",
        typeId.getGraphId(),
        typeId.getId());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<TypeId, Long> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from node_sequence where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(TypeId typeId) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from node_sequence where graph_id = ? and type_id = ?",
        Long.class,
        typeId.getGraphId(),
        typeId.getId()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(TypeId typeId, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from node_sequence where graph_id = ? and type_id = ?",
        mapper,
        typeId.getGraphId(),
        typeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<TypeId> buildKeyMapper() {
    return (rs, rowNum) -> new TypeId(
        rs.getString("type_id"),
        UUIDs.fromString(rs.getString("graph_id")));
  }

  @Override
  protected RowMapper<Long> buildValueMapper() {
    return (rs, rowNum) -> rs.getLong("value");
  }

}
