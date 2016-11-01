package fi.thl.termed.service.node.internal;

import java.util.Objects;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class NodeReferenceAttributeNodesByValueId
    extends AbstractSqlSpecification<NodeAttributeValueId, NodeId> {

  private NodeId valueId;

  public NodeReferenceAttributeNodesByValueId(NodeId valueId) {
    this.valueId = valueId;
  }

  @Override
  public boolean test(NodeAttributeValueId key, NodeId value) {
    return Objects.equals(value, valueId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "value_graph_id = ? and value_type_id = ? and value_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{valueId.getTypeGraphId(), valueId.getTypeId(), valueId.getId()};
  }

}
