package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

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
        "insert into text_attribute_property (text_attribute_domain_graph_id, text_attribute_domain_id, text_attribute_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
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
        "update text_attribute_property set lang = ?, value = ? where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
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
        "delete from text_attribute_property where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<PropertyValueId<TextAttributeId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from text_attribute_property where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }


  @Override
  public boolean exists(PropertyValueId<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForOptional(
        "select count(*) from text_attribute_property where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        Long.class,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<TextAttributeId> id,
      RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getSubjectId();
    TypeId textAttributeDomainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForFirst(
        "select * from text_attribute_property where text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and property_id = ? and index = ?",
        mapper,
        textAttributeDomainId.getGraphId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected RowMapper<PropertyValueId<TextAttributeId>> buildKeyMapper() {
    return (rs, rowNum) -> {

      TypeId domainId = TypeId.of(rs.getString("text_attribute_domain_id"),
          GraphId.fromUuidString(rs.getString("text_attribute_domain_graph_id")));

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
