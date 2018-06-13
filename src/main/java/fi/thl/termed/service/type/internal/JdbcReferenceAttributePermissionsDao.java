package fi.thl.termed.service.type.internal;

import static com.google.common.base.Preconditions.checkArgument;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcReferenceAttributePermissionsDao
    extends AbstractJdbcDao2<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> {

  public JdbcReferenceAttributePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<ReferenceAttributeId> id, GrantedPermission value) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    checkArgument(Objects.equals(id.getGraph(), referenceAttributeDomainId.getGraph()));

    jdbcTemplate.update(
        "insert into reference_attribute_permission (reference_attribute_domain_graph_id, reference_attribute_domain_id, reference_attribute_id, role, permission) values (?, ?, ?, ?, ?)",
        referenceAttributeDomainId.getGraphId(),
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
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    checkArgument(Objects.equals(id.getGraph(), referenceAttributeDomainId.getGraph()));

    jdbcTemplate.update(
        "delete from reference_attribute_permission where reference_attribute_domain_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and role = ? and permission = ?",
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from reference_attribute_permission where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute_permission where reference_attribute_domain_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and reference_attribute_domain_graph_id = ? and role = ? and permission = ?",
        Long.class,
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<ReferenceAttributeId> id,
      RowMapper<E> mapper) {
    ReferenceAttributeId referenceAttributeId = id.getObjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();
    return jdbcTemplate.query(
        "select * from reference_attribute_permission where reference_attribute_domain_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and reference_attribute_domain_graph_id = ? and role = ? and permission = ?",
        mapper,
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()).stream().findFirst();
  }

  @Override
  protected RowMapper<ObjectRolePermission<ReferenceAttributeId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      GraphId graphId = GraphId.fromUuidString(rs.getString("reference_attribute_domain_graph_id"));
      TypeId domainId = new TypeId(rs.getString("reference_attribute_domain_id"), graphId);
      ReferenceAttributeId referenceAttributeId = new ReferenceAttributeId(
          domainId, rs.getString("reference_attribute_id"));

      return new ObjectRolePermission<>(
          referenceAttributeId,
          new GraphRole(graphId, rs.getString("role")),
          Permission.valueOf(rs.getString("permission")));
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return (rs, rowNum) -> GrantedPermission.INSTANCE;
  }

}
