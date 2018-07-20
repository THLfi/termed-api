package fi.thl.termed.service.revision.internal;

import fi.thl.termed.domain.Revision;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcRevisionDao extends AbstractJdbcDao<Long, Revision> {

  public JdbcRevisionDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(Long id, Revision value) {
    jdbcTemplate.update("insert into revision (number, author, date) values (?, ?, ?)",
        id, value.getAuthor(), value.getDate());
  }

  @Override
  public void update(Long id, Revision value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Long id) {
    jdbcTemplate.update("delete from revision where number = ?", id);
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<Long, Revision> specification, RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from revision where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(Long id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from revision where number = ?", Long.class, id)
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(Long id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from revision where number = ?", mapper, id).stream().findFirst();
  }

  @Override
  protected RowMapper<Long> buildKeyMapper() {
    return (rs, rowNum) -> rs.getLong("number");
  }

  @Override
  protected RowMapper<Revision> buildValueMapper() {
    return (rs, rowNum) -> Revision
        .of(rs.getLong("number"), rs.getString("author"), rs.getTimestamp("date"));
  }

}
