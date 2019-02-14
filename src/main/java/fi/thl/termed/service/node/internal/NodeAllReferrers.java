package fi.thl.termed.service.node.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class NodeAllReferrers implements LuceneSpecification<NodeId, Node> {

  private NodeId objectId;

  private NodeAllReferrers(NodeId objectId) {
    this.objectId = objectId;
  }

  public static NodeAllReferrers of(NodeId objectId) {
    return new NodeAllReferrers(objectId);
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));

    for (NodeId reference : node.getReferences().values()) {
      if (Objects.equals(reference, objectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("references.nodeId", objectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeAllReferrers that = (NodeAllReferrers) o;
    return Objects.equals(objectId, that.objectId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .toString();
  }

}
