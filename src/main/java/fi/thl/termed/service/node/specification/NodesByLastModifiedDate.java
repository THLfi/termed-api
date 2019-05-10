package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.DateUtils;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import fi.thl.termed.util.query.SqlSpecification;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class NodesByLastModifiedDate
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final LocalDateTime lower;
  private final LocalDateTime upper;

  public NodesByLastModifiedDate(LocalDateTime lower, LocalDateTime upper) {
    this.lower = lower;
    this.upper = upper;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    Preconditions.checkNotNull(node.getLastModifiedDate());

    LocalDateTime modified = node.getLastModifiedDate();

    return (lower == null || modified.equals(lower) || modified.isAfter(lower)) &&
        (upper == null || modified.equals(upper) || modified.isBefore(upper));
  }

  @Override
  public Query luceneQuery() {
    return new TermRangeQuery("lastModifiedDate",
        lower != null ? new BytesRef(DateUtils.formatLuceneDateString(lower)) : null,
        upper != null ? new BytesRef(DateUtils.formatLuceneDateString(upper)) : null,
        true, true);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    List<String> clauses = new ArrayList<>();
    List<Object> params = new ArrayList<>();

    if (lower != null) {
      clauses.add("last_modified_date >= ?");
      params.add(Timestamp.valueOf(lower));
    }
    if (upper != null) {
      clauses.add("last_modified_date <= ?");
      params.add(Timestamp.valueOf(upper));
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
    NodesByLastModifiedDate that = (NodesByLastModifiedDate) o;
    return Objects.equals(lower, that.lower) &&
        Objects.equals(upper, that.upper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lower, upper);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("lower", lower)
        .add("upper", upper)
        .toString();
  }

}
