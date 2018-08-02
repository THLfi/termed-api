package fi.thl.termed.service.node.internal;

import com.google.common.base.Strings;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeDao extends AbstractJdbcDao<NodeId, Node> {

  public JdbcNodeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(NodeId nodeId, Node node) {
    jdbcTemplate.update(
        "insert into node (graph_id, type_id, id, code, uri, number, created_by, created_date, last_modified_by, last_modified_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        node.getCode().map(Strings::emptyToNull).orElse(null),
        node.getUri().map(Strings::emptyToNull).orElse(null),
        node.getNumber(),
        node.getCreatedBy(),
        node.getCreatedDate(),
        node.getLastModifiedBy(),
        node.getLastModifiedDate());
  }

  @Override
  public void update(NodeId nodeId, Node node) {
    jdbcTemplate.update(
        "update node set code = ?, uri = ?, number = ?, created_by = ?, created_date = ?, last_modified_by = ?, last_modified_date = ? where graph_id = ? and type_id = ? and id = ?",
        node.getCode().map(Strings::emptyToNull).orElse(null),
        node.getUri().map(Strings::emptyToNull).orElse(null),
        node.getNumber(),
        node.getCreatedBy(),
        node.getCreatedDate(),
        node.getLastModifiedBy(),
        node.getLastModifiedDate(),
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId());
  }

  @Override
  public void delete(NodeId nodeId) {
    jdbcTemplate.update(
        "delete from node where graph_id = ? and type_id = ? and id = ?",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId());
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<NodeId, Node> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from node where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(NodeId nodeId) {
    return jdbcTemplate.queryForOptional(
        "select count(*) from node where graph_id = ? and type_id = ? and id = ?",
        Long.class,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId())
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(NodeId nodeId, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from node where graph_id = ? and type_id = ? and id = ?",
        mapper,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<NodeId> buildKeyMapper() {
    return (rs, rowNum) -> new NodeId(UUIDs.fromString(rs.getString("id")),
        rs.getString("type_id"),
        UUIDs.fromString(rs.getString("graph_id")));
  }

  @Override
  protected RowMapper<Node> buildValueMapper() {
    return (rs, rowNum) -> Node.builder()
        .id(UUIDs.fromString(rs.getString("id")),
            TypeId.of(rs.getString("type_id"), GraphId.fromUuidString(rs.getString("graph_id"))))
        .code(rs.getString("code"))
        .uri(rs.getString("uri"))
        .number(rs.getLong("number"))
        .createdBy(rs.getString("created_by"))
        .createdDate(rs.getTimestamp("created_date"))
        .lastModifiedBy(rs.getString("last_modified_by"))
        .lastModifiedDate(rs.getTimestamp("last_modified_date"))
        .build();
  }

}
