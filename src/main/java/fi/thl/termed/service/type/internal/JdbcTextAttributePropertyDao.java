package fi.thl.termed.service.type.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcTextAttributePropertyDao
    extends AbstractJdbcDao<PropertyValueId<TextAttributeId>, LangValue> {

  public JdbcTextAttributePropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<TextAttributeId> id, LangValue langValue) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into text_attribute_property (text_attribute_graph_id, text_attribute_domain_id, text_attribute_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
        textAttributeDomainId.getGraphId(),
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
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "update text_attribute_property set lang = ?, value = ? where text_attribute_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from text_attribute_property where text_attribute_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from text_attribute_property", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<PropertyValueId<TextAttributeId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from text_attribute_property where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }


  @Override
  public boolean exists(PropertyValueId<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute_property where text_attribute_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        Long.class,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<TextAttributeId> id,
                                RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    return jdbcTemplate.query(
        "select * from text_attribute_property where text_attribute_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        mapper,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<TextAttributeId>> buildKeyMapper() {
    return (rs, rowNum) -> {

      TypeId domainId = new TypeId(rs.getString("text_attribute_domain_id"),
                                   UUIDs.fromString(rs.getString("text_attribute_graph_id"))
      );

      TextAttributeId textAttributeId =
          new TextAttributeId(domainId, rs.getString("text_attribute_id"));

      return new PropertyValueId<>(
          textAttributeId,
          rs.getString("property_id"),
          rs.getInt("index"));
    };
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
