package fi.thl.termed.service.node.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcNodeReferenceAttributeValueDao
    extends AbstractJdbcDao<NodeAttributeValueId, NodeId> {

  public JdbcNodeReferenceAttributeValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(NodeAttributeValueId id, NodeId value) {
    NodeId nodeId = id.getNodeId();

    jdbcTemplate.update(
        "insert into node_reference_attribute_value (graph_id, node_type_id, node_id, attribute_id, index, value_graph_id, value_type_id, value_id) values (?, ?, ?, ?, ?, ?, ?, ?)",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex(),
        value.getTypeGraphId(),
        value.getTypeId(),
        value.getId());
  }

  @Override
  public void update(NodeAttributeValueId id, NodeId value) {
    NodeId nodeId = id.getNodeId();

    jdbcTemplate.update(
        "update node_reference_attribute_value set value_graph_id = ?, value_type_id = ?, value_id = ? where graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        value.getTypeGraphId(),
        value.getTypeId(),
        value.getId(),
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex());
  }

  @Override
  public void delete(NodeAttributeValueId id) {
    NodeId nodeId = id.getNodeId();

    jdbcTemplate.update(
        "delete from node_reference_attribute_value where graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from node_reference_attribute_value", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<NodeAttributeValueId, NodeId> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from node_reference_attribute_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(NodeAttributeValueId id) {
    NodeId nodeId = id.getNodeId();

    return jdbcTemplate.queryForObject(
        "select count(*) from node_reference_attribute_value where graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        Long.class,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex()) > 0;
  }

  @Override
  protected <E> Optional<E> get(NodeAttributeValueId id, RowMapper<E> mapper) {
    NodeId nodeId = id.getNodeId();

    return jdbcTemplate.query(
        "select * from node_reference_attribute_value where graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        mapper,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex()).stream().findFirst();
  }

  @Override
  protected RowMapper<NodeAttributeValueId> buildKeyMapper() {
    return (rs, rowNum) -> new NodeAttributeValueId(
        new NodeId(UUIDs.fromString(rs.getString("node_id")),
                   rs.getString("node_type_id"), UUIDs.fromString(rs.getString("graph_id"))),
        rs.getString("attribute_id"),
        rs.getInt("index")
    );
  }

  @Override
  protected RowMapper<NodeId> buildValueMapper() {
    return (rs, rowNum) -> new NodeId(UUIDs.fromString(rs.getString("value_id")),
                                      rs.getString("value_type_id"),
                                      UUIDs.fromString(rs.getString("value_graph_id"))
    );
  }

}
