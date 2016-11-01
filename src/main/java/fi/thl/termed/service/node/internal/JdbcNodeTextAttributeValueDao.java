package fi.thl.termed.service.node.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcNodeTextAttributeValueDao
    extends AbstractJdbcDao<NodeAttributeValueId, StrictLangValue> {

  public JdbcNodeTextAttributeValueDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(NodeAttributeValueId id, StrictLangValue langValue) {
    NodeId nodeId = id.getNodeId();

    jdbcTemplate.update(
        "insert into node_text_attribute_value (node_graph_id, node_type_id, node_id, attribute_id, index, lang, value, regex) values (?, ?, ?, ?, ?, ?, ?, ?)",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex(),
        langValue.getLang(),
        langValue.getValue(),
        langValue.getRegex());
  }

  @Override
  public void update(NodeAttributeValueId id, StrictLangValue langValue) {
    NodeId nodeId = id.getNodeId();

    jdbcTemplate.update(
        "update node_text_attribute_value set lang = ?, value = ?, regex = ? where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        langValue.getLang(),
        langValue.getValue(),
        langValue.getRegex(),
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
        "delete from node_text_attribute_value where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        id.getAttributeId(),
        id.getIndex());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from node_text_attribute_value", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<NodeAttributeValueId, StrictLangValue> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from node_text_attribute_value where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(NodeAttributeValueId id) {
    NodeId nodeId = id.getNodeId();

    return jdbcTemplate.queryForObject(
        "select count(*) from node_text_attribute_value where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
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
        "select * from node_text_attribute_value where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ?",
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
                   rs.getString("node_type_id"), UUIDs.fromString(rs.getString("node_graph_id"))),
        rs.getString("attribute_id"),
        rs.getInt("index")
    );
  }

  @Override
  protected RowMapper<StrictLangValue> buildValueMapper() {
    return (rs, rowNum) -> new StrictLangValue(rs.getString("lang"),
                                               rs.getString("value"),
                                               rs.getString("regex"));
  }

}
