package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcClassPropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<ClassId>, LangValue> {

  public JdbcClassPropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<ClassId> id, LangValue langValue) {
    ClassId classId = id.getSubjectId();

    jdbcTemplate.update(
        "insert into class_property_value (class_scheme_id, class_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?)",
        classId.getSchemeId(),
        classId.getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<ClassId> id, LangValue langValue) {
    ClassId classId = id.getSubjectId();

    jdbcTemplate.update(
        "update class_property_value set lang = ?, value = ? where class_scheme_id = ? and class_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        classId.getSchemeId(),
        classId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<ClassId> id) {
    ClassId classId = id.getSubjectId();

    jdbcTemplate.update(
        "delete from class_property_value where class_scheme_id = ? and class_id = ? and property_id = ? and index = ?",
        classId.getSchemeId(),
        classId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from class_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<PropertyValueId<ClassId>, LangValue> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from class_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(PropertyValueId<ClassId> id) {
    ClassId classId = id.getSubjectId();

    return jdbcTemplate.queryForObject(
        "select count(*) from class_property_value where class_scheme_id = ? and class_id = ? and property_id = ? and index = ?",
        Long.class,
        classId.getSchemeId(),
        classId.getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<ClassId> id, RowMapper<E> mapper) {
    ClassId classId = id.getSubjectId();

    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from class_property_value where class_scheme_id = ? and class_id = ? and property_id = ? and index = ?",
        mapper,
        classId.getSchemeId(),
        classId.getId(),
        id.getPropertyId(),
        id.getIndex()));
  }

  @Override
  protected RowMapper<PropertyValueId<ClassId>> buildKeyMapper() {
    return new RowMapper<PropertyValueId<ClassId>>() {
      public PropertyValueId<ClassId> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PropertyValueId<ClassId>(
            new ClassId(UUIDs.fromString(rs.getString("class_scheme_id")),
                        rs.getString("class_id")),
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
