package fi.thl.termed.service.scheme.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcSchemePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<SchemeId>, GrantedPermission> {

  public JdbcSchemePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<SchemeId> id, GrantedPermission value) {
    jdbcTemplate.update(
        "insert into scheme_permission (scheme_id, role, permission) values (?, ?, ?)",
        id.getObjectId().getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<SchemeId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<SchemeId> id) {
    jdbcTemplate.update(
        "delete from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        id.getObjectId().getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<SchemeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<SchemeId> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        Long.class,
        id.getObjectId().getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<SchemeId> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from scheme_permission where scheme_id = ? and role = ? and permission = ?",
        mapper,
        id.getObjectId().getId(),
        id.getRole(),
        id.getPermission().toString()).stream().findFirst();
  }

  @Override
  protected RowMapper<ObjectRolePermission<SchemeId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      SchemeId schemeId = new SchemeId(UUIDs.fromString(rs.getString("scheme_id")));
      return new ObjectRolePermission<>(schemeId,
                                        new SchemeRole(schemeId, rs.getString("role")),
                                        Permission.valueOf(rs.getString("permission")));
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return (rs, rowNum) -> GrantedPermission.INSTANCE;
  }

}
