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
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.SqlSpecification;

public class NodesByGraphId
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final UUID graphId;

  public NodesByGraphId(UUID graphId) {
    this.graphId = graphId;
  }

  public UUID getGraphId() {
    return graphId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(nodeId.getTypeGraphId(), graphId);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("type.graph.id", graphId.toString()));
  }

  @Override
  public String sqlQueryTemplate() {
    return "graph_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{graphId};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByGraphId that = (NodesByGraphId) o;
    return Objects.equals(graphId, that.graphId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("graphId", graphId)
        .toString();
  }

}
