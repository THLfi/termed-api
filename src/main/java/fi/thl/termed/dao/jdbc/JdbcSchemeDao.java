package fi.thl.termed.dao.jdbc;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

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
  protected <E> List<E> get(SqlSpecification<UUID, Scheme> specification, RowMapper<E> mapper) {
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
    return ListUtils.findFirst(jdbcTemplate.query("select * from scheme where id = ?", mapper, id));
  }

  @Override
  protected RowMapper<UUID> buildKeyMapper() {
    return new RowMapper<UUID>() {
      public UUID mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UUIDs.fromString(rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<Scheme> buildValueMapper() {
    return new RowMapper<Scheme>() {
      public Scheme mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Scheme(UUIDs.fromString(rs.getString("id")),
                          rs.getString("code"),
                          rs.getString("uri"));
      }
    };
  }

}
