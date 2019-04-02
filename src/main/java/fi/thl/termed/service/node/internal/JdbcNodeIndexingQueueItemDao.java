package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.IndexingQueueItemId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeIndexingQueueItemDao extends
    AbstractJdbcDao<IndexingQueueItemId<NodeId>, Empty> {

  public JdbcNodeIndexingQueueItemDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(IndexingQueueItemId<NodeId> id, Empty empty) {
    NodeId nodeId = id.getId();
    jdbcTemplate.update(
        "insert into node_indexing_queue_item ("
            + "node_graph_id,"
            + "node_type_id,"
            + "node_id,"
            + "node_indexing_queue_id) values (?, ?, ?, ?)",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getIndexingQueueId());
  }

  @Override
  public void update(IndexingQueueItemId<NodeId> id, Empty empty) {
    // NOP
  }

  @Override
  public void delete(IndexingQueueItemId<NodeId> id) {
    NodeId nodeId = id.getId();
    jdbcTemplate.update(
        "delete from node_indexing_queue_item where "
            + "node_graph_id = ? and "
            + "node_type_id = ? and"
            + "node_id = ? and"
            + "node_indexing_queue_id = ?",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getIndexingQueueId());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<IndexingQueueItemId<NodeId>, Empty> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String
            .format("select * from node_indexing_queue_item where %s",
                specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(IndexingQueueItemId<NodeId> id) {
    NodeId nodeId = id.getId();
    return jdbcTemplate.queryForOptional(
        "select count(*) from node_indexing_queue_item where "
            + "node_graph_id = ? and "
            + "node_type_id = ? and"
            + "node_id = ? and"
            + "node_indexing_queue_id = ?",
        Long.class,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getIndexingQueueId())
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(IndexingQueueItemId<NodeId> id, RowMapper<E> mapper) {
    NodeId nodeId = id.getId();
    return jdbcTemplate.queryForFirst(
        "select * from node_indexing_queue_item where "
            + "node_graph_id = ? and "
            + "node_type_id = ? and"
            + "node_id = ? and"
            + "node_indexing_queue_id = ?",
        mapper,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getIndexingQueueId());
  }

  @Override
  protected RowMapper<IndexingQueueItemId<NodeId>> buildKeyMapper() {
    return (rs, rowNum) -> IndexingQueueItemId.of(
        NodeId.of(
            UUIDs.fromString(rs.getString("node_id")),
            rs.getString("node_type_id"),
            UUIDs.fromString(rs.getString("node_graph_id"))),
        rs.getLong("node_indexing_queue_id"));
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
