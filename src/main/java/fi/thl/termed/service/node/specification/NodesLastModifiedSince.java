package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Date;
import java.util.Objects;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class NodesLastModifiedSince
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final Date date;

  public NodesLastModifiedSince(Date date) {
    this.date = date;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    Preconditions.checkNotNull(node.getLastModifiedDate());

    Date modified = node.getLastModifiedDate();

    return modified.equals(date) || modified.after(date);
  }

  @Override
  public Query luceneQuery() {
    return new TermRangeQuery("lastModifiedDate",
        new BytesRef(DateTools.dateToString(date, Resolution.MILLISECOND)),
        null, true, true);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("last_modified_date >= ?", date);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesLastModifiedSince that = (NodesLastModifiedSince) o;
    return Objects.equals(date, that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("date", date)
        .toString();
  }

}
