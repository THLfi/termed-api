package fi.thl.termed.service.node.internal;

import static java.util.Optional.ofNullable;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao2;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcPostgresNodeReferenceAttributeValueRevisionDao extends
    AbstractJdbcPostgresDao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> {

  public JdbcPostgresNodeReferenceAttributeValueRevisionDao(
      SystemDao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> delegate,
      DataSource dataSource) {
    super(delegate, dataSource, "node_reference_attribute_value_aud");
  }

  @Override
  protected String[] toRow(RevisionId<NodeAttributeValueId> k, Tuple2<RevisionType, NodeId> v) {
    NodeAttributeValueId nodeAttributeValueId = k.getId();
    NodeId nodeId = nodeAttributeValueId.getNodeId();

    RevisionType revisionType = v._1;
    Optional<NodeId> optionalNodeId = ofNullable(v._2);

    return new String[]{
        nodeId.getTypeGraphId().toString(),
        nodeId.getTypeId(),
        nodeId.getId().toString(),
        nodeAttributeValueId.getAttributeId(),
        nodeAttributeValueId.getIndex().toString(),
        optionalNodeId.map(NodeId::getTypeGraphId).map(Object::toString).orElse(null),
        optionalNodeId.map(NodeId::getTypeId).orElse(null),
        optionalNodeId.map(NodeId::getId).map(Object::toString).orElse(null),
        k.getRevision().toString(),
        revisionType.toString()
    };
  }

}
