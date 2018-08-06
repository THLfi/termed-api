package fi.thl.termed.service.graph.internal;

import static com.google.common.base.Preconditions.checkState;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcGraphRoleDao extends AbstractJdbcDao<GraphRole, Empty> {

  public JdbcGraphRoleDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(GraphRole id, Empty value) {
    jdbcTemplate.update(
        "insert into graph_role (graph_id, role) values (?, ?)",
        id.getGraphId(), id.getRole());
  }

  @Override
  public void update(GraphRole id, Empty value) {
    // NOP (graph role doesn't have a separate value)
  }

  @Override
  public void delete(GraphRole id) {
    checkState(Objects.equals(id.getGraphId(), id.getGraphId()));

    jdbcTemplate.update(
        "delete from graph_role where graph_id = ? and role = ?",
        id.getGraphId(), id.getRole());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<GraphRole, Empty> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from graph_role where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(GraphRole id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from graph_role where graph_id = ? and role = ?",
        Long.class,
        id.getGraphId(),
        id.getRole()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(GraphRole id, RowMapper<E> mapper) {
    return jdbcTemplate.queryForFirst(
        "select * from graph_role where graph_id = ? and role = ?",
        mapper,
        id.getGraphId(),
        id.getRole());
  }

  @Override
  protected RowMapper<GraphRole> buildKeyMapper() {
    return (rs, rowNum) -> new GraphRole(new GraphId(UUIDs.fromString(rs.getString("graph_id"))),
        rs.getString("role"));
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
