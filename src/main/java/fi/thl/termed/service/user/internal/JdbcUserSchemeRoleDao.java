package fi.thl.termed.service.user.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.UserSchemeRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcUserSchemeRoleDao extends AbstractJdbcDao<UserSchemeRole, Empty> {

  public JdbcUserSchemeRoleDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(UserSchemeRole id, Empty value) {
    jdbcTemplate.update(
        "insert into user_scheme_role (username, scheme_id, role) values (?, ?, ?)",
        id.getUsername(), id.getSchemeId(), id.getRole());
  }

  @Override
  public void update(UserSchemeRole id, Empty value) {
    // NOP (user scheme role doesn't have a separate value)
  }

  @Override
  public void delete(UserSchemeRole id) {
    jdbcTemplate.update(
        "delete from user_scheme_role where username = ? and scheme_id = ? and role = ?",
        id.getUsername(), id.getSchemeId(), id.getRole());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from user_scheme_role", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<UserSchemeRole, Empty> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from user_scheme_role where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UserSchemeRole id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from user_scheme_role where username = ? and scheme_id = ? and role = ?",
        Long.class,
        id.getUsername(),
        id.getSchemeId(),
        id.getRole()) > 0;
  }

  @Override
  protected <E> Optional<E> get(UserSchemeRole id, RowMapper<E> mapper) {
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from user_scheme_role where username = ? and scheme_id = ? and role = ?",
        mapper,
        id.getUsername(),
        id.getSchemeId(),
        id.getRole()));
  }

  @Override
  protected RowMapper<UserSchemeRole> buildKeyMapper() {
    return (rs, rowNum) -> new UserSchemeRole(
        rs.getString("username"),
        new SchemeId(UUIDs.fromString(rs.getString("scheme_id"))),
        rs.getString("role"));
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
