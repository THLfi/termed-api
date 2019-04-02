package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeIndexingQueueDao extends AbstractJdbcDao<Long, Empty> {

  public JdbcNodeIndexingQueueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(Long id, Empty empty) {
    jdbcTemplate.update("insert into node_indexing_queue (id) values (?)", id);
  }

  @Override
  public void update(Long id, Empty indexed) {
    // NOP
  }

  @Override
  public void delete(Long id) {
    jdbcTemplate.update("delete from node_indexing_queue where id = ?", id);
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<Long, Empty> specification, RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String
            .format("select * from node_indexing_queue where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(Long id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from node_indexing_queue where id = ?",
        Long.class,
        id)
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(Long id, RowMapper<E> mapper) {
    return jdbcTemplate.queryForFirst(
        "select * from node_indexing_queue where id = ?",
        mapper,
        id);
  }

  @Override
  protected RowMapper<Long> buildKeyMapper() {
    return (rs, rowNum) -> rs.getLong("id");
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
