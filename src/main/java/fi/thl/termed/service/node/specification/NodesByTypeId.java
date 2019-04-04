package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class NodesByTypeId
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final String typeId;

  public NodesByTypeId(String typeId) {
    Preconditions.checkArgument(typeId.matches(RegularExpressions.CODE));
    this.typeId = typeId;
  }

  public static NodesByTypeId of(String typeId) {
    return new NodesByTypeId(typeId);
  }

  public String getTypeId() {
    return typeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(nodeId.getTypeId(), typeId);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("type.id", typeId));
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("type_id = ?", typeId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByTypeId that = (NodesByTypeId) o;
    return Objects.equals(typeId, that.typeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("typeId", typeId)
        .toString();
  }

}
