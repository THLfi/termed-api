package fi.thl.termed.service.type.internal;

import static java.lang.String.format;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTypeDao extends AbstractJdbcDao<TypeId, Type> {

  public JdbcTypeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TypeId typeId, Type newType) {
    jdbcTemplate.update("insert into type (graph_id, id, uri, index) values (?, ?, ?, ?)",
        typeId.getGraphId(),
        typeId.getId(),
        newType.getUri().orElse(null),
        newType.getIndex().orElse(null));
  }

  @Override
  public void update(TypeId typeId, Type newType) {
    jdbcTemplate.update("update type set uri = ?, index = ? where graph_id = ? and id = ?",
        newType.getUri().orElse(null),
        newType.getIndex().orElse(null),
        typeId.getGraphId(),
        typeId.getId());
  }

  @Override
  public void delete(TypeId typeId) {
    jdbcTemplate.update("delete from type where graph_id = ? and id = ?",
        typeId.getGraphId(),
        typeId.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from type", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<TypeId, Type> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        format("select * from type where %s order by index", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(TypeId typeId) {
    return jdbcTemplate.queryForObject("select count(*) from type where graph_id = ? and id = ?",
        Long.class, typeId.getGraphId(), typeId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(TypeId typeId, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from type where graph_id = ? and id = ?",
        mapper, typeId.getGraphId(), typeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<TypeId> buildKeyMapper() {
    return (rs, rowNum) -> TypeId.of(rs.getString("id"),
        GraphId.fromUuidString(rs.getString("graph_id"))
    );
  }

  @Override
  protected RowMapper<Type> buildValueMapper() {
    return (rs, rowNum) -> Type.builder()
        .id(rs.getString("id"), UUIDs.fromString(rs.getString("graph_id")))
        .uri(rs.getString("uri"))
        .index(rs.getInt("index"))
        .build();
  }

}
