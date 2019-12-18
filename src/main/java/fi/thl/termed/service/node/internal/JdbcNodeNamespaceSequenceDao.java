package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeNamespaceSequenceDao extends AbstractJdbcDao<Tuple2<GraphId, String>, Long> {

  public JdbcNodeNamespaceSequenceDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(Tuple2<GraphId, String> graphIdNamespace, Long value) {
    GraphId graphId = graphIdNamespace._1;
    String namespace = graphIdNamespace._2;

    jdbcTemplate.update(
        "insert into node_namespace_sequence (graph_id, namespace, value) values (?, ?, ?)",
        graphId.getId(),
        namespace,
        value);
  }

  @Override
  public void update(Tuple2<GraphId, String> graphIdNamespace, Long value) {
    GraphId graphId = graphIdNamespace._1;
    String namespace = graphIdNamespace._2;

    jdbcTemplate.update(
        "update node_namespace_sequence set value = ? where graph_id = ? and namespace = ?",
        value,
        graphId.getId(),
        namespace);
  }

  @Override
  public void delete(Tuple2<GraphId, String> graphIdNamespace) {
    GraphId graphId = graphIdNamespace._1;
    String namespace = graphIdNamespace._2;

    jdbcTemplate.update(
        "delete from node_namespace_sequence where graph_id = ? and namespace = ?",
        graphId.getId(),
        namespace);
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<Tuple2<GraphId, String>, Long> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from node_namespace_sequence where %s",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(Tuple2<GraphId, String> graphIdNamespace) {
    GraphId graphId = graphIdNamespace._1;
    String namespace = graphIdNamespace._2;

    return jdbcTemplate.queryForOptional(
        "select count(*) from node_namespace_sequence where graph_id = ? and namespace = ?",
        Long.class,
        graphId.getId(),
        namespace).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(Tuple2<GraphId, String> graphIdNamespace, RowMapper<E> mapper) {
    GraphId graphId = graphIdNamespace._1;
    String namespace = graphIdNamespace._2;

    return jdbcTemplate.queryForFirst(
        "select * from node_namespace_sequence where graph_id = ? and namespace = ?",
        mapper,
        graphId.getId(),
        namespace);
  }

  @Override
  protected RowMapper<Tuple2<GraphId, String>> buildKeyMapper() {
    return (rs, rowNum) -> Tuple.of(
        GraphId.of(UUIDs.fromString(rs.getString("graph_id"))),
        rs.getString("namespace"));
  }

  @Override
  protected RowMapper<Long> buildValueMapper() {
    return (rs, rowNum) -> rs.getLong("value");
  }

}
