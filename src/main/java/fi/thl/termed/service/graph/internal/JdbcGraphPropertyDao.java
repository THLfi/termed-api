package fi.thl.termed.service.graph.internal;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcGraphPropertyDao extends AbstractJdbcDao<PropertyValueId<GraphId>, LangValue> {

  public JdbcGraphPropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<GraphId> id, LangValue langValue) {
    jdbcTemplate.update(
        "insert into graph_property (graph_id, property_id, index, lang, value) values (?, ?, ?, ?, ?)",
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<GraphId> id, LangValue langValue) {
    jdbcTemplate.update(
        "update graph_property set lang = ?, value = ? where graph_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<GraphId> id) {
    jdbcTemplate.update(
        "delete from graph_property where graph_id = ? and property_id = ? and index = ?",
        id.getSubjectId().getId(), id.getPropertyId(), id.getIndex());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<PropertyValueId<GraphId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from graph_property where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(PropertyValueId<GraphId> id) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from graph_property where graph_id = ? and property_id = ? and index = ?",
        Long.class,
        id.getSubjectId().getId(),
        id.getPropertyId(),
        id.getIndex()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<GraphId> id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from graph_property where graph_id = ? and property_id = ? and index = ?",
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
