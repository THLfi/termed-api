package fi.thl.termed.service.node.internal;

import static fi.thl.termed.domain.RevisionType.DELETE;
import static java.util.Optional.ofNullable;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcNodeTextAttributeValueRevisionDao extends
    AbstractJdbcDao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> {

  public JdbcNodeTextAttributeValueRevisionDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(RevisionId<NodeAttributeValueId> revisionId,
      Tuple2<RevisionType, StrictLangValue> revision) {

    NodeAttributeValueId nodeAttributeValueId = revisionId.getId();
    NodeId nodeId = nodeAttributeValueId.getNodeId();

    Optional<StrictLangValue> langValue = ofNullable(revision._2);

    jdbcTemplate.update(
        "insert into node_text_attribute_value_aud (node_graph_id, node_type_id, node_id, revision, attribute_id, index, lang, value, regex, revision_type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        revisionId.getRevision(),
        nodeAttributeValueId.getAttributeId(),
        nodeAttributeValueId.getIndex(),
        langValue.map(StrictLangValue::getLang).orElse(null),
        langValue.map(StrictLangValue::getValue).orElse(null),
        langValue.map(StrictLangValue::getRegex).orElse(null),
        revision._1.toString());
  }

  @Override
  public void update(RevisionId<NodeAttributeValueId> id,
      Tuple2<RevisionType, StrictLangValue> revision) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(RevisionId<NodeAttributeValueId> id) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected <E> Stream<E> get(
      SqlSpecification<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from node_text_attribute_value_aud where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(RevisionId<NodeAttributeValueId> revisionId) {
    NodeAttributeValueId nodeAttributeValueId = revisionId.getId();
    NodeId nodeId = nodeAttributeValueId.getNodeId();

    return jdbcTemplate.queryForOptional(
        "select count(*) from node_text_attribute_value_aud where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ? and revision = ?",
        Long.class,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        nodeAttributeValueId.getAttributeId(),
        nodeAttributeValueId.getIndex(),
        revisionId.getRevision())
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(RevisionId<NodeAttributeValueId> revisionId, RowMapper<E> mapper) {
    NodeAttributeValueId nodeAttributeValueId = revisionId.getId();
    NodeId nodeId = nodeAttributeValueId.getNodeId();

    return jdbcTemplate.queryForFirst(
        "select * from node_text_attribute_value_aud where node_graph_id = ? and node_type_id = ? and node_id = ? and attribute_id = ? and index = ? and revision = ?",
        mapper,
        nodeId.getTypeGraphId(),
        nodeId.getTypeId(),
        nodeId.getId(),
        nodeAttributeValueId.getAttributeId(),
        nodeAttributeValueId.getIndex(),
        revisionId.getRevision());
  }

  @Override
  protected RowMapper<RevisionId<NodeAttributeValueId>> buildKeyMapper() {
    return (rs, rowNum) -> RevisionId.of(
        new NodeAttributeValueId(
            new NodeId(
                UUIDs.fromString(rs.getString("node_id")),
                rs.getString("node_type_id"),
                UUIDs.fromString(rs.getString("node_graph_id"))),
            rs.getString("attribute_id"),
            rs.getInt("index")),
        rs.getLong("revision"));
  }

  @Override
  protected RowMapper<Tuple2<RevisionType, StrictLangValue>> buildValueMapper() {
    return (rs, rowNum) -> {
      RevisionType revisionType = RevisionType.valueOf(rs.getString("revision_type"));

      if (revisionType == DELETE) {
        return Tuple.of(revisionType, null);
      }

      return Tuple.of(
          RevisionType.valueOf(rs.getString("revision_type")),
          new StrictLangValue(rs.getString("lang"),
              rs.getString("value"),
              rs.getString("regex")));
    };
  }

}
