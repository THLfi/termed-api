package fi.thl.termed.service.user.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;

public class JdbcUserGraphRoleDao extends AbstractJdbcDao<UserGraphRole, Empty> {

  public JdbcUserGraphRoleDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(UserGraphRole id, Empty value) {
    jdbcTemplate.update(
        "insert into user_graph_role (username, graph_id, role) values (?, ?, ?)",
        id.getUsername(), id.getGraphId(), id.getRole());
  }

  @Override
  public void update(UserGraphRole id, Empty value) {
    // NOP (user graph role doesn't have a separate value)
  }

  @Override
  public void delete(UserGraphRole id) {
    jdbcTemplate.update(
        "delete from user_graph_role where username = ? and graph_id = ? and role = ?",
        id.getUsername(), id.getGraphId(), id.getRole());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from user_graph_role", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<UserGraphRole, Empty> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from user_graph_role where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UserGraphRole id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from user_graph_role where username = ? and graph_id = ? and role = ?",
        Long.class,
        id.getUsername(),
        id.getGraphId(),
        id.getRole()) > 0;
  }

  @Override
  protected <E> Optional<E> get(UserGraphRole id, RowMapper<E> mapper) {
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from user_graph_role where username = ? and graph_id = ? and role = ?",
        mapper,
        id.getUsername(),
        id.getGraphId(),
        id.getRole()));
  }

  @Override
  protected RowMapper<UserGraphRole> buildKeyMapper() {
    return (rs, rowNum) -> new UserGraphRole(
        rs.getString("username"),
        new GraphId(UUIDs.fromString(rs.getString("graph_id"))),
        rs.getString("role"));
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
