package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class NodesByTypeId
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final String typeId;

  public NodesByTypeId(String typeId) {
    Preconditions.checkArgument(typeId.matches(RegularExpressions.CODE));
    this.typeId = typeId;
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
  public String sqlQueryTemplate() {
    return "type_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId};
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
