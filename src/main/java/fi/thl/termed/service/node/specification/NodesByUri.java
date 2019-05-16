package fi.thl.termed.service.node.specification;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class NodesByUri
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private String uri;

  public NodesByUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(node.getUri().orElse(null), uri);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("uri", uri));
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("uri = ?", uri);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByUri that = (NodesByUri) o;
    return Objects.equals(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return "uri = " + uri;
  }

}
