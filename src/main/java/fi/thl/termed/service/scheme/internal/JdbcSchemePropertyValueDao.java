package fi.thl.termed.service.scheme.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcSchemePropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<SchemeId>, LangValue> {

  public JdbcSchemePropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<SchemeId> id, LangValue langValue) {
    jdbcTemplate.update(
        "insert into scheme_property_value (scheme_id, property_id, index, lang, value) values (?, ?, ?, ?, ?)",
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<SchemeId> id, LangValue langValue) {
    jdbcTemplate.update(
        "update scheme_property_value set lang = ?, value = ? where scheme_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<SchemeId> id) {
    jdbcTemplate.update(
        "delete from scheme_property_value where scheme_id = ? and property_id = ? and index = ?",
        id.getSubjectId().getId(), id.getPropertyId(), id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<PropertyValueId<SchemeId>, LangValue> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(PropertyValueId<SchemeId> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from scheme_property_value where scheme_id = ? and property_id = ? and index = ?",
        Long.class,
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<SchemeId> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from scheme_property_value where scheme_id = ? and property_id = ? and index = ?",
        mapper,
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<SchemeId>> buildKeyMapper() {
    return (rs, rowNum) -> new PropertyValueId<>(
        new SchemeId(UUIDs.fromString(rs.getString("scheme_id"))),
        rs.getString("property_id"),
        rs.getInt("index"));
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
