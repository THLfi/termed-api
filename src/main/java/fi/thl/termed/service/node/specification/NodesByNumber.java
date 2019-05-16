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

public class NodesByNumber
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private Long number;

  public NodesByNumber(Long number) {
    this.number = number;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(node.getNumber(), number);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("number", String.valueOf(number)));
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("number = ?", number);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByNumber that = (NodesByNumber) o;
    return Objects.equals(number, that.number);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }

  @Override
  public String toString() {
    return "number = " + number;
  }

}
