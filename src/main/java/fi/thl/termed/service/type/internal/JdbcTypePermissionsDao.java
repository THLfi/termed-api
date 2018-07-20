package fi.thl.termed.service.type.internal;

import static com.google.common.base.Preconditions.checkArgument;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTypePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<TypeId>, GrantedPermission> {

  public JdbcTypePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<TypeId> id, GrantedPermission value) {
    TypeId typeId = id.getObjectId();

    checkArgument(Objects.equals(id.getGraph(), typeId.getGraph()));

    jdbcTemplate.update(
        "insert into type_permission (type_graph_id, type_id, role, permission) values (?, ?, ?, ?)",
        typeId.getGraphId(), typeId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<TypeId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<TypeId> id) {
    TypeId typeId = id.getObjectId();

    checkArgument(Objects.equals(id.getGraph(), typeId.getGraph()));

    jdbcTemplate.update(
        "delete from type_permission where type_graph_id = ? and type_id = ? and role = ? and permission = ?",
        typeId.getGraphId(), typeId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<ObjectRolePermission<TypeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from type_permission where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<TypeId> id) {
    TypeId typeId = id.getObjectId();
    return jdbcTemplate.queryForOptional(
        "select count(*) from type_permission where type_graph_id = ? and type_id = ? and type_graph_id = ? and role = ? and permission = ?",
        Long.class,
        typeId.getGraphId(),
        typeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString())
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<TypeId> id, RowMapper<E> mapper) {
    TypeId typeId = id.getObjectId();
    return jdbcTemplate.query(
        "select * from type_permission where type_graph_id = ? and type_id = ? and type_graph_id = ? and role = ? and permission = ?",
        mapper,
        typeId.getGraphId(),
        typeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()).stream().findFirst();
  }

  @Override
  protected RowMapper<ObjectRolePermission<TypeId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      GraphId graphId = new GraphId(UUIDs.fromString(rs.getString("type_graph_id")));
      TypeId typeId = new TypeId(rs.getString("type_id"), graphId);
      return new ObjectRolePermission<>(
          typeId, new GraphRole(graphId, rs.getString("role")),
          Permission.valueOf(rs.getString("permission")));
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return (rs, rowNum) -> GrantedPermission.INSTANCE;
  }

}
