package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.spesification.sql.SqlSpecification;
import fi.thl.termed.util.UUIDs;

public class JdbcResourceDao extends AbstractJdbcDao<ResourceId, Resource> {

  public JdbcResourceDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ResourceId resourceId, Resource resource) {
    jdbcTemplate.update(
        "insert into resource (scheme_id, type_id, id, code, uri, created_by, created_date, last_modified_by, last_modified_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        resourceId.getSchemeId(),
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
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId());
  }

  @Override
  public void delete(ResourceId resourceId) {
    jdbcTemplate.update(
        "delete from resource where scheme_id = ? and type_id = ? and id = ?",
        resourceId.getSchemeId(),
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
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId()) > 0;
  }

  @Override
  protected <E> E get(ResourceId resourceId, RowMapper<E> mapper) {
    return Iterables.getFirst(jdbcTemplate.query(
        "select * from resource where scheme_id = ? and type_id = ? and id = ?",
        mapper,
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId()), null);
  }

  @Override
  protected RowMapper<ResourceId> buildKeyMapper() {
    return new RowMapper<ResourceId>() {
      public ResourceId mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ResourceId(UUIDs.fromString(rs.getString("scheme_id")),
                              rs.getString("type_id"),
                              UUIDs.fromString(rs.getString("id")));
      }
    };
  }

  @Override
  protected RowMapper<Resource> buildValueMapper() {
    return new RowMapper<Resource>() {
      public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
        Resource resource = new Resource(UUIDs.fromString(rs.getString("id")));
        resource.setScheme(new Scheme(UUIDs.fromString(rs.getString("scheme_id"))));
        resource.setType(new Class(rs.getString("type_id")));

        resource.setCode(rs.getString("code"));
        resource.setUri(rs.getString("uri"));

        resource.setCreatedBy(rs.getString("created_by"));
        resource.setCreatedDate(rs.getTimestamp("created_date"));
        resource.setLastModifiedBy(rs.getString("last_modified_by"));
        resource.setLastModifiedDate(rs.getTimestamp("last_modified_date"));

        return resource;
      }
    };
  }

}
