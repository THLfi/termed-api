package fi.thl.termed.service.node.specification;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

public class NodesWithoutReferrers implements LuceneSpecification<NodeId, Node> {

  private String attributeId;

  public NodesWithoutReferrers(String attributeId) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    this.attributeId = attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return !node.getReferrers().containsKey(attributeId);
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery.Builder query = new BooleanQuery.Builder();
    query.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
    query.add(new TermRangeQuery("referrers." + attributeId + ".nodeId", null, null, true, true),
        BooleanClause.Occur.MUST_NOT);
    return query.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesWithoutReferrers that = (NodesWithoutReferrers) o;
    return Objects.equals(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId);
  }

  @Override
  public String toString() {
    return "referrers." + attributeId + ".id = null";

  }

}
