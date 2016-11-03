package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;
import java.util.Objects;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.specification.LuceneSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class NodesByTextAttributeValuePrefix
    implements LuceneSpecification<NodeId, Node> {

  private final TextAttributeId attributeId;
  private final String value;

  public NodesByTextAttributeValuePrefix(TextAttributeId attributeId, String value) {
    this.attributeId = attributeId;
    this.value = value;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));

    if (Objects.equals(new TypeId(nodeId), attributeId.getDomainId())) {
      for (Map.Entry<String, StrictLangValue> entry : node.getProperties().entries()) {
        StrictLangValue attributeValue = entry.getValue();

        if (Objects.equals(entry.getKey(), attributeId.getId()) &&
            attributeValue.getValue().startsWith(value)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    TypeId domainId = attributeId.getDomainId();
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("type.graph.id", domainId.getGraphId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", domainId.getId())), MUST);
    query.add(new PrefixQuery(new Term(attributeId.getId(), value)), MUST);
    return query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByTextAttributeValuePrefix that = (NodesByTextAttributeValuePrefix) o;
    return Objects.equals(attributeId, that.attributeId) &&
           Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("value", value)
        .toString();
  }

}
