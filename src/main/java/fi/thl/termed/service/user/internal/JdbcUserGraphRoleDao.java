package fi.thl.termed.service.user.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

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
  protected <E> Stream<E> get(SqlSpecification<UserGraphRole, Empty> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from user_graph_role where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UserGraphRole id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from user_graph_role where username = ? and graph_id = ? and role = ?",
        Long.class,
        id.getUsername(),
        id.getGraphId(),
        id.getRole()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(UserGraphRole id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from user_graph_role where username = ? and graph_id = ? and role = ?",
        mapper,
        id.getUsername(),
        id.getGraphId(),
        id.getRole()).stream().findFirst();
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
