package fi.thl.termed.repository.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.repository.dao.ResourceTextAttributeValueDao;
import fi.thl.termed.repository.spesification.SqlSpecification;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.UUIDs;

@Repository
public class JdbcResourceTextAttributeValueDao
    extends AbstractJdbcDao<ResourceAttributeValueId, LangValue>
    implements ResourceTextAttributeValueDao {

  @Autowired
  public JdbcResourceTextAttributeValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ResourceAttributeValueId id, LangValue langValue) {
    ResourceId resourceId = id.getResourceId();

    jdbcTemplate.update(
        "insert into resource_text_attribute_value (scheme_id, resource_type_id, resource_id, attribute_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        id.getAttributeId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(ResourceAttributeValueId id, LangValue langValue) {
    ResourceId resourceId = id.getResourceId();

    jdbcTemplate.update(
        "update resource_text_attribute_value set lang = ?, value = ? where scheme_id = ? and resource_type_id = ? and resource_id = ? and attribute_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        id.getAttributeId(),
        id.getIndex());
  }

  @Override
  public void delete(ResourceAttributeValueId id) {
    ResourceId resourceId = id.getResourceId();

    jdbcTemplate.update(
        "delete from resource_text_attribute_value where scheme_id = ? and resource_type_id = ? and resource_id = ? and attribute_id = ? and index = ?",
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        id.getAttributeId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from resource_text_attribute_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<ResourceAttributeValueId, LangValue> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from resource_text_attribute_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(ResourceAttributeValueId id) {
    ResourceId resourceId = id.getResourceId();

    return jdbcTemplate.queryForObject(
        "select count(*) from resource_text_attribute_value where scheme_id = ? and resource_type_id = ? and resource_id = ? and attribute_id = ? and index = ?",
        Long.class,
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        id.getAttributeId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> E get(ResourceAttributeValueId id, RowMapper<E> mapper) {
    ResourceId resourceId = id.getResourceId();

    return Iterables.getFirst(jdbcTemplate.query(
        "select * from resource_text_attribute_value scheme_id = ? and resource_type_id = ? and resource_id = ? and attribute_id = ? and index = ?",
        mapper,
        resourceId.getSchemeId(),
        resourceId.getTypeId(),
        resourceId.getId(),
        id.getAttributeId(),
        id.getIndex()), null);
  }

  @Override
  protected RowMapper<ResourceAttributeValueId> buildKeyMapper() {
    return new RowMapper<ResourceAttributeValueId>() {
      public ResourceAttributeValueId mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ResourceAttributeValueId(
            new ResourceId(UUIDs.fromString(rs.getString("scheme_id")),
                           rs.getString("resource_type_id"),
                           UUIDs.fromString(rs.getString("resource_id"))),
            rs.getString("attribute_id"),
            rs.getInt("index")
        );
      }
    };
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return new LangValueRowMapper();
  }

}
