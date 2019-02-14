package fi.thl.termed.service.node.internal;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class NodeAllReferences implements LuceneSpecification<NodeId, Node> {

  private NodeId subjectId;

  private NodeAllReferences(NodeId subjectId) {
    this.subjectId = requireNonNull(subjectId);
  }

  public static NodeAllReferences of(NodeId subjectId) {
    return new NodeAllReferences(subjectId);
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));

    for (NodeId referrer : node.getReferrers().values()) {
      if (Objects.equals(referrer, subjectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("referrers.nodeId", subjectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeAllReferences that = (NodeAllReferences) o;
    return Objects.equals(subjectId, that.subjectId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subjectId", subjectId)
        .toString();
  }

}
