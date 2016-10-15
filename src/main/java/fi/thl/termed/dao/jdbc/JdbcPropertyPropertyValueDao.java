package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.collect.ListUtils;

public class JdbcPropertyPropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<String>, LangValue> {

  public JdbcPropertyPropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<String> id, LangValue langValue) {
    jdbcTemplate.update(
        "insert into property_property_value (subject_id, property_id, index, lang, value) values (?, ?, ?, ?, ?)",
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<String> id, LangValue langValue) {
    jdbcTemplate.update(
        "update property_property_value set lang = ?, value = ? where subject_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<String> id) {
    jdbcTemplate.update(
        "delete from property_property_value where subject_id = ? and property_id = ? and index = ?",
        id.getSubjectId(), id.getPropertyId(), id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from property_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<PropertyValueId<String>, LangValue> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from property_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(PropertyValueId<String> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from property_property_value where subject_id = ? and property_id = ? and index = ?",
        Long.class,
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<String> id, RowMapper<E> mapper) {
    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from property_property_value where subject_id = ? and property_id = ? and index = ?",
        mapper,
        id.getSubjectId(),
        id.getPropertyId(),
        id.getIndex()));
  }

  @Override
  protected RowMapper<PropertyValueId<String>> buildKeyMapper() {
    return new RowMapper<PropertyValueId<String>>() {
      public PropertyValueId<String> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PropertyValueId<String>(rs.getString("subject_id"),
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
