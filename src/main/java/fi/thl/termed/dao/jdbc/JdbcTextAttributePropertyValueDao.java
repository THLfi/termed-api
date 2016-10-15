package fi.thl.termed.dao.jdbc;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcTextAttributePropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<TextAttributeId>, LangValue> {

  public JdbcTextAttributePropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<TextAttributeId> id, LangValue langValue) {
    TextAttributeId textAttributeId = id.getSubjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into text_attribute_property_value (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<TextAttributeId> id, LangValue langValue) {
    TextAttributeId textAttributeId = id.getSubjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "update text_attribute_property_value set lang = ?, value = ? where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getSubjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from text_attribute_property_value where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from text_attribute_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<PropertyValueId<TextAttributeId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from text_attribute_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }


  @Override
  public boolean exists(PropertyValueId<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getSubjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute_property_value where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        Long.class,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<TextAttributeId> id, RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getSubjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();

    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from text_attribute_property_value where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        mapper,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()));
  }

  @Override
  protected RowMapper<PropertyValueId<TextAttributeId>> buildKeyMapper() {
    return new RowMapper<PropertyValueId<TextAttributeId>>() {
      public PropertyValueId<TextAttributeId> mapRow(ResultSet rs, int rowNum) throws SQLException {

        ClassId domainId = new ClassId(UUIDs.fromString(rs.getString("text_attribute_scheme_id")),
                                       rs.getString("text_attribute_domain_id"));

        TextAttributeId textAttributeId =
            new TextAttributeId(domainId, rs.getString("text_attribute_id"));

        return new PropertyValueId<TextAttributeId>(
            textAttributeId,
            rs.getString("property_id"),
            rs.getInt("index"));
      }
    };
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return new LangValueRowMapper();
  }

}
