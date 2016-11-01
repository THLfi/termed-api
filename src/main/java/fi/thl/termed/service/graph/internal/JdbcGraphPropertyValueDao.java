package fi.thl.termed.service.graph.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcGraphPropertyValueDao
    extends AbstractJdbcDao<PropertyValueId<GraphId>, LangValue> {

  public JdbcGraphPropertyValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<GraphId> id, LangValue langValue) {
    jdbcTemplate.update(
        "insert into graph_property_value (graph_id, property_id, index, lang, value) values (?, ?, ?, ?, ?)",
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<GraphId> id, LangValue langValue) {
    jdbcTemplate.update(
        "update graph_property_value set lang = ?, value = ? where graph_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<GraphId> id) {
    jdbcTemplate.update(
        "delete from graph_property_value where graph_id = ? and property_id = ? and index = ?",
        id.getSubjectId().getId(), id.getPropertyId(), id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from graph_property_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<PropertyValueId<GraphId>, LangValue> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from graph_property_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(PropertyValueId<GraphId> id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from graph_property_value where graph_id = ? and property_id = ? and index = ?",
        Long.class,
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<GraphId> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from graph_property_value where graph_id = ? and property_id = ? and index = ?",
        mapper,
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<GraphId>> buildKeyMapper() {
    return (rs, rowNum) -> new PropertyValueId<>(
        new GraphId(UUIDs.fromString(rs.getString("graph_id"))),
        rs.getString("property_id"),
        rs.getInt("index"));
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
