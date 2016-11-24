package fi.thl.termed.service.node.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;
import java.util.Objects;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;

public class NodeReferrers implements LuceneSpecification<NodeId, Node> {

  private NodeId objectId;
  private String attrId;

  public NodeReferrers(NodeId objectId, String attrId) {
    Preconditions.checkArgument(attrId.matches(RegularExpressions.CODE));
    this.objectId = objectId;
    this.attrId = attrId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));

    for (Map.Entry<String, NodeId> entry : node.getReferences().entries()) {
      if (Objects.equals(entry.getKey(), attrId) &&
          Objects.equals(entry.getValue(), objectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("references." + attrId + ".nodeId", objectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeReferrers that = (NodeReferrers) o;
    return Objects.equals(objectId, that.objectId) &&
           Objects.equals(attrId, that.attrId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectId, attrId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .add("attrId", attrId)
        .toString();
  }

}
