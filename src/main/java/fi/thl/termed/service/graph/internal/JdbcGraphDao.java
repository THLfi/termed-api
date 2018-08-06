package fi.thl.termed.service.graph.internal;

import com.google.common.base.Strings;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcGraphDao extends AbstractJdbcDao<GraphId, Graph> {

  public JdbcGraphDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(GraphId id, Graph graph) {
    jdbcTemplate.update("insert into graph (id, code, uri) values (?, ?, ?)",
        id.getId(),
        graph.getCode().map(Strings::emptyToNull).orElse(null),
        graph.getUri().map(Strings::emptyToNull).orElse(null));
  }

  @Override
  public void update(GraphId id, Graph graph) {
    jdbcTemplate.update("update graph set code = ?, uri = ? where id = ?",
        graph.getCode().map(Strings::emptyToNull).orElse(null),
        graph.getUri().map(Strings::emptyToNull).orElse(null),
        id.getId());
  }

  @Override
  public void delete(GraphId id) {
    jdbcTemplate.update("delete from graph where id = ?", id.getId());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<GraphId, Graph> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from graph where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(GraphId id) {
    return jdbcTemplate.queryForOptional("select count(*) from graph where id = ?",
        Long.class, id.getId()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(GraphId id, RowMapper<E> mapper) {
    return jdbcTemplate.queryForFirst("select * from graph where id = ?", mapper, id.getId());
  }

  @Override
  protected RowMapper<GraphId> buildKeyMapper() {
    return (rs, rowNum) -> GraphId.fromUuidString(rs.getString("id"));
  }

  @Override
  protected RowMapper<Graph> buildValueMapper() {
    return (rs, rowNum) -> Graph.builder()
        .id(GraphId.fromUuidString(rs.getString("id")))
        .code(rs.getString("code"))
        .uri(rs.getString("uri"))
        .build();
  }

}
