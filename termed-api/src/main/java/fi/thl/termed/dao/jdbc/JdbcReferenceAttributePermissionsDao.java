package fi.thl.termed.dao.jdbc;

import com.google.common.base.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcReferenceAttributePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> {

  public JdbcReferenceAttributePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<ReferenceAttributeId> id, GrantedPermission value) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    jdbcTemplate.update(
        "insert into reference_attribute_permission (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id, role, permission) values (?, ?, ?, ?, ?)",
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<ReferenceAttributeId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    jdbcTemplate.update(
        "delete from reference_attribute_permission where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and role = ? and permission = ?",
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from reference_attribute_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from reference_attribute_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute_permission where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and role = ? and permission = ?",
        Long.class,
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<ReferenceAttributeId> id,
                                RowMapper<E> mapper) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from reference_attribute_permission where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and role = ? and permission = ?",
        mapper,
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()));
  }

  @Override
  protected RowMapper<ObjectRolePermission<ReferenceAttributeId>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<ReferenceAttributeId>>() {
      @Override
      public ObjectRolePermission<ReferenceAttributeId> mapRow(ResultSet rs, int rowNum)
          throws SQLException {

        UUID schemeId = UUIDs.fromString(rs.getString("reference_attribute_scheme_id"));
        ClassId domainId = new ClassId(schemeId, rs.getString("reference_attribute_domain_id"));
        ReferenceAttributeId referenceAttributeId = new ReferenceAttributeId(
            domainId, rs.getString("reference_attribute_id"));

        return new ObjectRolePermission<ReferenceAttributeId>(
            referenceAttributeId,
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
