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

public class NodeReferences implements LuceneSpecification<NodeId, Node> {

  private NodeId subjectId;
  private String attrId;

  public NodeReferences(NodeId subjectId, String attrId) {
    Preconditions.checkArgument(attrId.matches(RegularExpressions.CODE));
    this.subjectId = subjectId;
    this.attrId = attrId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));

    for (Map.Entry<String, NodeId> entry : node.getReferrers().entries()) {
      if (Objects.equals(entry.getKey(), attrId) &&
          Objects.equals(entry.getValue(), subjectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("referrers." + attrId + ".nodeId", subjectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeReferences that = (NodeReferences) o;
    return Objects.equals(subjectId, that.subjectId) &&
           Objects.equals(attrId, that.attrId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectId, attrId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subjectId", subjectId)
        .add("attrId", attrId)
        .toString();
  }

}
