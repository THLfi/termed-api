package fi.thl.termed.dao.jdbc;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcReferenceAttributePropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<ReferenceAttributeId>, LangValue> {

  public JdbcReferenceAttributePropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<ReferenceAttributeId> id, LangValue langValue) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into reference_attribute_property_value (reference_attribute_scheme_id, reference_attribute_domain_id, reference_attribute_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?, ?)",
        referenceAttributeDomainId.getSchemeId(),
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
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "update reference_attribute_property_value set lang = ?, value = ? where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<ReferenceAttributeId> id) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from reference_attribute_property_value where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        referenceAttributeDomainId.getSchemeId(),
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
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute_property_value where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        Long.class,
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<ReferenceAttributeId> id, RowMapper<E> mapper) {
    ReferenceAttributeId referenceAttributeId = id.getSubjectId();
    ClassId referenceAttributeDomainId = referenceAttributeId.getDomainId();

    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from reference_attribute_property_value where reference_attribute_scheme_id = ? and reference_attribute_domain_id = ? and reference_attribute_id = ? and property_id = ? and index = ?",
        mapper,
        referenceAttributeDomainId.getSchemeId(),
        referenceAttributeDomainId.getId(),
        referenceAttributeId.getId(),
        id.getPropertyId(),
        id.getIndex()));
  }

  @Override
  protected RowMapper<PropertyValueId<ReferenceAttributeId>> buildKeyMapper() {
    return new RowMapper<PropertyValueId<ReferenceAttributeId>>() {
      public PropertyValueId<ReferenceAttributeId> mapRow(ResultSet rs, int rowNum)
          throws SQLException {

        ClassId domainId =
            new ClassId(UUIDs.fromString(rs.getString("reference_attribute_scheme_id")),
                        rs.getString("reference_attribute_domain_id"));

        ReferenceAttributeId referenceAttributeId =
            new ReferenceAttributeId(domainId, rs.getString("reference_attribute_id"));

        return new PropertyValueId<ReferenceAttributeId>(
            referenceAttributeId,
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
