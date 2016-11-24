package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;

public class NodesByReference implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final UUID valueNodeId;

  public NodesByReference(String attributeId, UUID valueNodeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    this.attributeId = attributeId;
    this.valueNodeId = valueNodeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getReferences().get(attributeId).stream()
        .anyMatch(v -> v.getId().equals(valueNodeId));
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("references." + attributeId + ".id", valueNodeId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByReference that = (NodesByReference) o;
    return Objects.equals(attributeId, that.attributeId) &&
           Objects.equals(valueNodeId, that.valueNodeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, valueNodeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("valueNodeId", valueNodeId)
        .toString();
  }

}
