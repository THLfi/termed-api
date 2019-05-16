package fi.thl.termed.service.node.specification;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.Query;

public class NodesByNumberRange
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final Long lower;
  private final Long upper;

  public NodesByNumberRange(Long lower, Long upper) {
    this.lower = lower;
    this.upper = upper;
  }

  public static NodesByNumberRange of(Long lower, Long upper) {
    return new NodesByNumberRange(lower, upper);
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    Preconditions.checkNotNull(node.getNumber());

    Long number = node.getNumber();

    return (lower == null || number >= lower)
        && (upper == null || number <= upper);
  }

  @Override
  public Query luceneQuery() {
    return LongPoint.newRangeQuery("number",
        lower != null ? lower : Long.MIN_VALUE,
        upper != null ? upper : Long.MAX_VALUE);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    List<String> clauses = new ArrayList<>();
    List<Object> params = new ArrayList<>();

    if (lower != null) {
      clauses.add("number >= ?");
      params.add(lower);
    }
    if (upper != null) {
      clauses.add("number <= ?");
      params.add(upper);
    }

    if (clauses.isEmpty()) {
      return ParametrizedSqlQuery.of("1 = 1");
    }

    return ParametrizedSqlQuery.of(
        String.join(" AND ", clauses),
        params.toArray(new Object[0]));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByNumberRange that = (NodesByNumberRange) o;
    return Objects.equals(lower, that.lower) &&
        Objects.equals(upper, that.upper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lower, upper);
  }

  @Override
  public String toString() {
    return sql().toString();
  }

}
