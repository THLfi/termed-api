package fi.thl.termed.service.property.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcPropertyPropertyDao extends AbstractJdbcDao2<PropertyValueId<String>, LangValue> {

  public JdbcPropertyPropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<String> id, LangValue langValue) {
    jdbcTemplate.update(
        "insert into property_property (subject_id, property_id, index, lang, value) values (?, ?, ?, ?, ?)",
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<String> id, LangValue langValue) {
    jdbcTemplate.update(
        "update property_property set lang = ?, value = ? where subject_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<String> id) {
    jdbcTemplate.update(
        "delete from property_property where subject_id = ? and property_id = ? and index = ?",
        id.getSubjectId(), id.getPropertyId(), id.getIndex());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<PropertyValueId<String>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from property_property where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(PropertyValueId<String> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from property_property where subject_id = ? and property_id = ? and index = ?",
        Long.class,
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<String> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from property_property where subject_id = ? and property_id = ? and index = ?",
        mapper,
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<String>> buildKeyMapper() {
    return (rs, rowNum) -> new PropertyValueId<>(rs.getString("subject_id"),
        rs.getString("property_id"),
        rs.getInt("index"));
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
