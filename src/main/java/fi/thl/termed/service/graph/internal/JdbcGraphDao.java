package fi.thl.termed.service.graph.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;

import static com.google.common.base.Strings.emptyToNull;

public class JdbcGraphDao extends AbstractJdbcDao<GraphId, Graph> {

  public JdbcGraphDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(GraphId id, Graph graph) {
    jdbcTemplate.update("insert into graph (id, code, uri) values (?, ?, ?)",
                        id.getId(), emptyToNull(graph.getCode()), emptyToNull(graph.getUri()));
  }

  @Override
  public void update(GraphId id, Graph graph) {
    jdbcTemplate.update("update graph set code = ?, uri = ? where id = ?",
                        emptyToNull(graph.getCode()), emptyToNull(graph.getUri()), id.getId());
  }

  @Override
  public void delete(GraphId id) {
    jdbcTemplate.update("delete from graph where id = ?", id.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from graph", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<GraphId, Graph> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from graph where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(GraphId id) {
    return jdbcTemplate.queryForObject("select count(*) from graph where id = ?",
                                       Long.class, id.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(GraphId id, RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from graph where id = ?", mapper, id.getId()).stream()
        .findFirst();
  }

  @Override
  protected RowMapper<GraphId> buildKeyMapper() {
    return (rs, rowNum) -> new GraphId(UUIDs.fromString(rs.getString("id")));
  }

  @Override
  protected RowMapper<Graph> buildValueMapper() {
    return (rs, rowNum) -> new Graph(UUIDs.fromString(rs.getString("id")),
                                     rs.getString("code"),
                                     rs.getString("uri"));
  }

}
