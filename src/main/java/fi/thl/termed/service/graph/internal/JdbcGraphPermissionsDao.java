package fi.thl.termed.service.graph.internal;

import static com.google.common.base.Preconditions.checkArgument;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcGraphPermissionsDao
    extends AbstractJdbcDao2<ObjectRolePermission<GraphId>, GrantedPermission> {

  public JdbcGraphPermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<GraphId> id, GrantedPermission value) {
    checkArgument(Objects.equals(id.getGraph(), id.getObjectId()));

    jdbcTemplate.update(
        "insert into graph_permission (graph_id, role, permission) values (?, ?, ?)",
        id.getObjectId().getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<GraphId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<GraphId> id) {
    checkArgument(Objects.equals(id.getGraph(), id.getObjectId()));

    jdbcTemplate.update(
        "delete from graph_permission where graph_id = ? and role = ? and permission = ?",
        id.getObjectId().getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<ObjectRolePermission<GraphId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from graph_permission where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<GraphId> id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from graph_permission where graph_id = ? and graph_id = ? and role = ? and permission = ?",
        Long.class,
        id.getObjectId().getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<GraphId> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from graph_permission where graph_id = ? and graph_id = ? and role = ? and permission = ?",
        mapper,
        id.getObjectId().getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()).stream().findFirst();
  }

  @Override
  protected RowMapper<ObjectRolePermission<GraphId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      GraphId graphId = GraphId.fromUuidString(rs.getString("graph_id"));
      return new ObjectRolePermission<>(graphId,
          new GraphRole(graphId, rs.getString("role")),
          Permission.valueOf(rs.getString("permission")));
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return (rs, rowNum) -> GrantedPermission.INSTANCE;
  }

}
