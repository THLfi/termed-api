package fi.thl.termed.service.type.internal;

import static com.google.common.base.Preconditions.checkArgument;

import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTextAttributePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<TextAttributeId>, GrantedPermission> {

  public JdbcTextAttributePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<TextAttributeId> id, GrantedPermission value) {
    TextAttributeId textAttributeId = id.getObjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    checkArgument(Objects.equals(id.getGraph(), textAttributeDomainId.getGraph()));

    jdbcTemplate.update(
        "insert into text_attribute_permission (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id, role, permission) values (?, ?, ?, ?, ?)",
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<TextAttributeId> id, GrantedPermission value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    checkArgument(Objects.equals(id.getGraph(), textAttributeDomainId.getGraph()));

    jdbcTemplate.update(
        "delete from text_attribute_permission where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<ObjectRolePermission<TextAttributeId>, GrantedPermission> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from text_attribute_permission where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();
    return jdbcTemplate.queryForOptional(
        "select count(*) from text_attribute_permission where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and text_attribute_domain_graph_id = ? and role = ? and permission = ?",
        Long.class,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString())
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(ObjectRolePermission<TextAttributeId> id, RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getObjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();
    return jdbcTemplate.query(
        "select * from text_attribute_permission where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and text_attribute_domain_graph_id = ? and role = ? and permission = ?",
        mapper,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getGraphId(),
        id.getRole(),
        id.getPermission().toString()).stream().findFirst();
  }

  @Override
  protected RowMapper<ObjectRolePermission<TextAttributeId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      GraphId graphId = GraphId.fromUuidString(rs.getString("text_attribute_domain_graph_id"));
      TypeId domainId = new TypeId(rs.getString("text_attribute_domain_id"), graphId);
      TextAttributeId textAttributeId = new TextAttributeId(
          domainId, rs.getString("text_attribute_id"));

      return new ObjectRolePermission<>(
          textAttributeId, new GraphRole(graphId, rs.getString("role")),
          Permission.valueOf(rs.getString("permission")));
    };
  }

  @Override
  protected RowMapper<GrantedPermission> buildValueMapper() {
    return (rs, rowNum) -> GrantedPermission.INSTANCE;
  }

}
