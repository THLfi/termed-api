package fi.thl.termed.service.scheme.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcSchemeDao extends AbstractJdbcDao<UUID, Scheme> {

  public JdbcSchemeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(UUID id, Scheme scheme) {
    jdbcTemplate.update("insert into scheme (id, code, uri) values (?, ?, ?)",
                        id, scheme.getCode(), scheme.getUri());
  }

  @Override
  public void update(UUID id, Scheme scheme) {
    jdbcTemplate.update("update scheme set code = ?, uri = ? where id = ?",
                        scheme.getCode(), scheme.getUri(), id);
  }

  @Override
  public void delete(UUID id) {
    jdbcTemplate.update("delete from scheme where id = ?", id);
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<UUID, Scheme> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UUID id) {
    return jdbcTemplate.queryForObject("select count(*) from scheme where id = ?",
                                       Long.class, id) > 0;
  }

  @Override
  protected <E> Optional<E> get(UUID id, RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme where id = ?", mapper, id).stream().findFirst();
  }

  @Override
  protected RowMapper<UUID> buildKeyMapper() {
    return (rs, rowNum) -> UUIDs.fromString(rs.getString("id"));
  }

  @Override
  protected RowMapper<Scheme> buildValueMapper() {
    return (rs, rowNum) -> new Scheme(UUIDs.fromString(rs.getString("id")),
                                      rs.getString("code"),
                                      rs.getString("uri"));
  }

}
