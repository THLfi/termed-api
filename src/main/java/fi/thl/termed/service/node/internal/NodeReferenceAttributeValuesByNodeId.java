package fi.thl.termed.service.node.internal;

import java.util.Objects;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class NodeReferenceAttributeValuesByNodeId
    extends AbstractSqlSpecification<NodeAttributeValueId, NodeId> {

  private NodeId nodeId;

  public NodeReferenceAttributeValuesByNodeId(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public boolean test(NodeAttributeValueId attributeValueId, NodeId value) {
    return Objects.equals(attributeValueId.getNodeId(), nodeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "node_graph_id = ? and node_type_id = ? and node_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{nodeId.getTypeGraphId(), nodeId.getTypeId(), nodeId.getId()};
  }

}
