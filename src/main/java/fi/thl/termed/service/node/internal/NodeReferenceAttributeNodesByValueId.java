package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

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
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of(
        "value_graph_id = ? and value_type_id = ? and value_id = ?",
        valueId.getTypeGraphId(), valueId.getTypeId(), valueId.getId());
  }

}
