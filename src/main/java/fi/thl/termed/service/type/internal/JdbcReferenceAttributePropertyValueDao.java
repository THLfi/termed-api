package fi.thl.termed.service.type.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcReferenceAttributePropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<ReferenceAttributeId>, LangValue> {

  public JdbcReferenceAttributePropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<ReferenceAttributeId> id, LangValue langValue) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into reference_attribute_property_value (reference_attribute_graph_id, reference_attribute_domain_id, reference_attribute_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<ReferenceAttributeId> id, LangValue langValue) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "update reference_attribute_property_value set lang = ?, value = ? where reference_attribute_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from reference_attribute_property_value where reference_attribute_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from reference_attribute_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<PropertyValueId<ReferenceAttributeId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from reference_attribute_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(PropertyValueId<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute_property_value where reference_attribute_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        Long.class,
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<ReferenceAttributeId> id,
                                RowMapper<E> mapper) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    TypeId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.query(
        "select * from reference_attribute_property_value where reference_attribute_graph_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        mapper,
        referenceAttributeDomainId.getGraphId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<ReferenceAttributeId>> buildKeyMapper() {
    return (rs, rowNum) -> {
      TypeId domainId =
          new TypeId(rs.getString("reference_attribute_domain_id"),
                     UUIDs.fromString(rs.getString("reference_attribute_graph_id"))
          );

      ReferenceAttributeId referenceAttributeId =
          new ReferenceAttributeId(domainId, rs.getString("reference_attribute_id"));

      return new PropertyValueId<>(
          referenceAttributeId,
          rs.getString("property_id"),
          rs.getInt("index"));
    };
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
