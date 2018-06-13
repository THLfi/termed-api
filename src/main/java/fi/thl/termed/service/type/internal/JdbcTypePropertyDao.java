package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTypePropertyDao extends AbstractJdbcDao2<PropertyValueId<TypeId>, LangValue> {

  public JdbcTypePropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(PropertyValueId<TypeId> id, LangValue langValue) {
    TypeId typeId = id.getSubjectId();

    jdbcTemplate.update(
        "insert into type_property (type_graph_id, type_id, property_id, index, lang, value) values (?, ?, ?, ?, ?, ?)",
        typeId.getGraphId(),
        typeId.getId(),
        id.getPropertyId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue());
  }

  @Override
  public void update(PropertyValueId<TypeId> id, LangValue langValue) {
    TypeId typeId = id.getSubjectId();

    jdbcTemplate.update(
        "update type_property set lang = ?, value = ? where type_graph_id = ? and type_id = ? and property_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        typeId.getGraphId(),
        typeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  public void delete(PropertyValueId<TypeId> id) {
    TypeId typeId = id.getSubjectId();

    jdbcTemplate.update(
        "delete from type_property where type_graph_id = ? and type_id = ? and property_id = ? and index = ?",
        typeId.getGraphId(),
        typeId.getId(),
        id.getPropertyId(),
        id.getIndex());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<PropertyValueId<TypeId>, LangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from type_property where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(PropertyValueId<TypeId> id) {
    TypeId typeId = id.getSubjectId();

    return jdbcTemplate.queryForOptional(
        "select count(*) from type_property where type_graph_id = ? and type_id = ? and property_id = ? and index = ?",
        Long.class,
        typeId.getGraphId(),
        typeId.getId(),
        id.getPropertyId(),
        id.getIndex()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(PropertyValueId<TypeId> id, RowMapper<E> mapper) {
    TypeId typeId = id.getSubjectId();

    return jdbcTemplate.query(
        "select * from type_property where type_graph_id = ? and type_id = ? and property_id = ? and index = ?",
        mapper,
        typeId.getGraphId(),
        typeId.getId(),
        id.getPropertyId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<PropertyValueId<TypeId>> buildKeyMapper() {
    return (rs, rowNum) -> new PropertyValueId<>(
        new TypeId(rs.getString("type_id"), UUIDs.fromString(rs.getString("type_graph_id"))),
        rs.getString("property_id"),
        rs.getInt("index"));
  }

  @Override
  protected RowMapper<LangValue> buildValueMapper() {
    return (rs, rowNum) -> new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
