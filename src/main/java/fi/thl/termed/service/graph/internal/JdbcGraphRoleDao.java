package fi.thl.termed.service.graph.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;

import static com.google.common.base.Preconditions.checkState;

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
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from graph_role", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<GraphRole, Empty> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from graph_role where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(GraphRole id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from graph_role where graph_id = ? and role = ?",
        Long.class,
        id.getGraphId(),
        id.getRole()) > 0;
  }

  @Override
  protected <E> Optional<E> get(GraphRole id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from graph_role where graph_id = ? and role = ?",
        mapper,
        id.getGraphId(),
        id.getRole()).stream().findFirst();
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
