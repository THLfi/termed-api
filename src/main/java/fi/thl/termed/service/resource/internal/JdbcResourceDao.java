package fi.thl.termed.service.resource.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcResourceDao extends AbstractJdbcDao<ResourceId, Resource> {

  public JdbcResourceDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ResourceId resourceId, Resource resource) {
    jdbcTemplate.update(
        "insert into resource (scheme_id, type_id, id, code, uri, created_by, created_date, last_modified_by, last_modified_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        resourceId.getTypeSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        resource.getCode(),
        resource.getUri(),
        resource.getCreatedBy(),
        resource.getCreatedDate(),
        resource.getLastModifiedBy(),
        resource.getLastModifiedDate());
  }

  @Override
  public void update(ResourceId resourceId, Resource resource) {
    jdbcTemplate.update(
        "update resource set code = ?, uri = ?, created_by = ?, created_date = ?, last_modified_by = ?, last_modified_date = ? where scheme_id = ? and type_id = ? and id = ?",
        resource.getCode(),
        resource.getUri(),
        resource.getCreatedBy(),
        resource.getCreatedDate(),
        resource.getLastModifiedBy(),
        resource.getLastModifiedDate(),
        resourceId.getTypeSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId());
  }

  @Override
  public void delete(ResourceId resourceId) {
    jdbcTemplate.update(
        "delete from resource where scheme_id = ? and type_id = ? and id = ?",
        resourceId.getTypeSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from resource", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<ResourceId, Resource> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from resource where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ResourceId resourceId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from resource where scheme_id = ? and type_id = ? and id = ?",
        Long.class,
        resourceId.getTypeSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ResourceId resourceId, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from resource where scheme_id = ? and type_id = ? and id = ?",
        mapper,
        resourceId.getTypeSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<ResourceId> buildKeyMapper() {
    return (rs, rowNum) -> new ResourceId(UUIDs.fromString(rs.getString("id")),
                                          rs.getString("type_id"),
                                          UUIDs.fromString(rs.getString("scheme_id"))
    );
  }

  @Override
  protected RowMapper<Resource> buildValueMapper() {
    return (rs, rowNum) -> {
      Resource resource = new Resource(UUIDs.fromString(rs.getString("id")));
      resource.setType(new ClassId(
          rs.getString("type_id"), UUIDs.fromString(rs.getString("scheme_id"))));

      resource.setCode(rs.getString("code"));
      resource.setUri(rs.getString("uri"));

      resource.setCreatedBy(rs.getString("created_by"));
      resource.setCreatedDate(rs.getTimestamp("created_date"));
      resource.setLastModifiedBy(rs.getString("last_modified_by"));
      resource.setLastModifiedDate(rs.getTimestamp("last_modified_date"));

      return resource;
    };
  }

}