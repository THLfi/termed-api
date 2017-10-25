package fi.thl.termed.service.node.internal;

import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcRevisionDao extends AbstractJdbcDao<Long, Void> {

  public JdbcRevisionDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(Long id, Void value) {
    jdbcTemplate.update("insert into revision (id) values (?)", id);
  }

  @Override
  public void update(Long id, Void value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Long id) {
    jdbcTemplate.update("delete from revision where id = ?", id);
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from revision", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<Long, Void> specification, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from revision where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(Long id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from revision where id = ?", Long.class, id) > 0;
  }

  @Override
  protected <E> Optional<E> get(Long id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from revision where id = ?", mapper, id).stream().findFirst();
  }

  @Override
  protected RowMapper<Long> buildKeyMapper() {
    return (rs, rowNum) -> rs.getLong("id");
  }

  @Override
  protected RowMapper<Void> buildValueMapper() {
    return (rs, rowNum) -> null;
  }

}
