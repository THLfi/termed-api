package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class NodesByLastModifiedDate
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final Date lower;
  private final Date upper;

  public NodesByLastModifiedDate(Date lower, Date upper) {
    this.lower = lower;
    this.upper = upper;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    Preconditions.checkNotNull(node.getLastModifiedDate());

    Date modified = node.getLastModifiedDate();

    return (lower == null || modified.equals(lower) || modified.after(lower)) &&
        (upper == null || modified.equals(upper) || modified.after(upper));
  }

  @Override
  public Query luceneQuery() {
    return new TermRangeQuery("lastModifiedDate",
        lower != null ? new BytesRef(DateTools.dateToString(lower, Resolution.MILLISECOND)) : null,
        upper != null ? new BytesRef(DateTools.dateToString(upper, Resolution.MILLISECOND)) : null,
        true, true);
  }

  @Override
  public String sqlQueryTemplate() {
    List<String> clauses = new ArrayList<>();

    if (lower != null) {
      clauses.add("last_modified_date >= ?");
    }
    if (upper != null) {
      clauses.add("last_modified_date <= ?");
    }

    return clauses.isEmpty() ? "1 = 1" : String.join(" AND ", clauses);
  }

  @Override
  public Object[] sqlQueryParameters() {
    List<Object> params = new ArrayList<>();

    if (lower != null) {
      params.add(lower);
    }
    if (upper != null) {
      params.add(upper);
    }

    return params.toArray(new Object[params.size()]);
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
