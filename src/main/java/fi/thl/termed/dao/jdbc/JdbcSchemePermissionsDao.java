package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcSchemePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<UUID>, GrantedPermission> {

  public JdbcSchemePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<UUID> id, GrantedPermission value) {
    jdbcTemplate.update(
        "insert into scheme_permission (scheme_id, role, permission) values (?, ?, ?)",
        id.getObjectId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<UUID> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<UUID> id) {
    jdbcTemplate.update(
        "delete from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        id.getObjectId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<UUID>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<UUID> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        Long.class,
        id.getObjectId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<UUID> id, RowMapper<E> mapper) {
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        mapper,
        id.getObjectId(),
        id.getRole(),
        id.getPermission().toString()));
  }

  @Override
  protected RowMapper<ObjectRolePermission<UUID>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<UUID>>() {
      @Override
      public ObjectRolePermission<UUID> mapRow(ResultSet rs, int rowNum) throws SQLException {
        UUID schemeId = UUIDs.fromString(rs.getString("scheme_id"));
        return new ObjectRolePermission<UUID>(schemeId,
                                              new SchemeRole(schemeId, rs.getString("role")),
                                              Permission.valueOf(rs.getString("permission")));
      }
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return new RowMapper<GrantedPermission>() {
      @Override
      public GrantedPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
        return GrantedPermission.INSTANCE;
      }
    };
  }

}
