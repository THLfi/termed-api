package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.UUIDs;

public class JdbcClassPermissionsDao extends AbstractJdbcDao<ObjectRolePermission<ClassId>, Void> {

  public JdbcClassPermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<ClassId> id, Void value) {
    ClassId classId = id.getObjectId();
    jdbcTemplate.update(
        "insert into class_permission (class_scheme_id, class_id, role, permission) values (?, ?, ?, ?)",
        classId.getSchemeId(), classId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<ClassId> id, Void value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<ClassId> id) {
    ClassId classId = id.getObjectId();
    jdbcTemplate.update(
        "delete from class_permission where class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        classId.getSchemeId(), classId.getId(), id.getRole(), id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from class_permission", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<ObjectRolePermission<ClassId>, Void> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from class_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<ClassId> id) {
    ClassId classId = id.getObjectId();
    return jdbcTemplate.queryForObject(
        "select count(*) from class_permission where class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        Long.class,
        classId.getSchemeId(),
        classId.getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> E get(ObjectRolePermission<ClassId> id, RowMapper<E> mapper) {
    ClassId classId = id.getObjectId();
    return Iterables.getFirst(jdbcTemplate.query(
        "select * from class_permission class_scheme_id = ? and class_id = ? and role = ? and permission = ?",
        mapper,
        classId.getSchemeId(),
        classId.getId(),
        id.getRole(),
        id.getPermission().toString()), null);
  }

  @Override
  protected RowMapper<ObjectRolePermission<ClassId>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<ClassId>>() {
      @Override
      public ObjectRolePermission<ClassId> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClassId classId =
            new ClassId(UUIDs.fromString(rs.getString("scheme_id")), rs.getString("class_id"));
        return new ObjectRolePermission<ClassId>(
            classId, rs.getString("role"), Permission.valueOf(rs.getString("permission")));
      }
    };
  }

  @Override
  protected RowMapper<Void> buildValueMapper() {
    return new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
      }
    };
  }

}
