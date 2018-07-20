package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao;
import javax.sql.DataSource;

public class JdbcPostgresNodeReferenceAttributeValueDao extends
    AbstractJdbcPostgresDao<NodeAttributeValueId, NodeId> {

  public JdbcPostgresNodeReferenceAttributeValueDao(
      SystemDao<NodeAttributeValueId, NodeId> delegate, DataSource dataSource) {
    super(delegate, dataSource, "node_reference_attribute_value");
  }

  @Override
  protected String[] toRow(NodeAttributeValueId k, NodeId v) {
    NodeId nodeId = k.getNodeId();

    return new String[]{
        nodeId.getTypeGraphId().toString(),
        nodeId.getTypeId(),
        nodeId.getId().toString(),
        k.getAttributeId(),
        k.getIndex().toString(),
        v.getTypeGraphId().toString(),
        v.getTypeId(),
        v.getId().toString()
    };
  }

}
