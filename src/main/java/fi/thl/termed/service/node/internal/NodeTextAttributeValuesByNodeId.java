package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class NodeTextAttributeValuesByNodeId
    extends AbstractSqlSpecification<NodeAttributeValueId, StrictLangValue> {

  private NodeId nodeId;

  public NodeTextAttributeValuesByNodeId(NodeId nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public boolean test(NodeAttributeValueId attributeValueId, StrictLangValue langValue) {
    return Objects.equals(attributeValueId.getNodeId(), nodeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("node_graph_id = ? and node_type_id = ? and node_id = ?",
        nodeId.getTypeGraphId(), nodeId.getTypeId(), nodeId.getId());
  }

}
