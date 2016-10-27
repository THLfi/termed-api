package fi.thl.termed.service.scheme.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcSchemeDao extends AbstractJdbcDao<SchemeId, Scheme> {

  public JdbcSchemeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(SchemeId id, Scheme scheme) {
    jdbcTemplate.update("insert into scheme (id, code, uri) values (?, ?, ?)",
                        id.getId(), scheme.getCode(), scheme.getUri());
  }

  @Override
  public void update(SchemeId id, Scheme scheme) {
    jdbcTemplate.update("update scheme set code = ?, uri = ? where id = ?",
                        scheme.getCode(), scheme.getUri(), id.getId());
  }

  @Override
  public void delete(SchemeId id) {
    jdbcTemplate.update("delete from scheme where id = ?", id.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<SchemeId, Scheme> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(SchemeId id) {
    return jdbcTemplate.queryForObject("select count(*) from scheme where id = ?",
                                       Long.class, id.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(SchemeId id, RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme where id = ?", mapper, id.getId()).stream()
        .findFirst();
  }

  @Override
  protected RowMapper<SchemeId> buildKeyMapper() {
    return (rs, rowNum) -> new SchemeId(UUIDs.fromString(rs.getString("id")));
  }

  @Override
  protected RowMapper<Scheme> buildValueMapper() {
    return (rs, rowNum) -> new Scheme(UUIDs.fromString(rs.getString("id")),
                                      rs.getString("code"),
                                      rs.getString("uri"));
  }

}
