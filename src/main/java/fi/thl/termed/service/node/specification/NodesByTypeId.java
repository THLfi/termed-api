package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class NodesByTypeId
    implements LuceneSpecification<NodeId, Node>, SqlSpecification<NodeId, Node> {

  private final TypeId typeId;

  public NodesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return Objects.equals(nodeId.getTypeGraphId(), typeId.getGraphId()) &&
           Objects.equals(nodeId.getTypeId(), typeId.getId());
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("type.graph.id", typeId.getGraphId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", typeId.getId())), MUST);
    return query;
  }

  @Override
  public String sqlQueryTemplate() {
    return "graph_id = ? and type_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
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
        .add("classId", typeId)
        .toString();
  }

}
