package fi.thl.termed.service.type.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;

import static com.google.common.base.Strings.emptyToNull;

public class JdbcTypeDao extends AbstractJdbcDao<TypeId, Type> {

  public JdbcTypeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TypeId typeId, Type newType) {
    jdbcTemplate.update("insert into type (graph_id, id, uri, index) values (?, ?, ?, ?)",
                        typeId.getGraphId(),
                        typeId.getId(),
                        emptyToNull(newType.getUri()),
                        newType.getIndex());
  }

  @Override
  public void update(TypeId typeId, Type newType) {
    jdbcTemplate.update("update type set uri = ?, index = ? where graph_id = ? and id = ?",
                        emptyToNull(newType.getUri()),
                        newType.getIndex(),
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
        String.format("select * from type where %s order by index",
                      specification.sqlQueryTemplate()),
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
    return (rs, rowNum) -> new TypeId(rs.getString("id"),
                                      UUIDs.fromString(rs.getString("graph_id"))
    );
  }

  @Override
  protected RowMapper<Type> buildValueMapper() {
    return (rs, rowNum) -> {
      Type cls = new Type(rs.getString("id"), rs.getString("uri"),
                          new GraphId(UUIDs.fromString(rs.getString("graph_id"))));
      cls.setIndex(rs.getInt("index"));
      return cls;
    };
  }

}
