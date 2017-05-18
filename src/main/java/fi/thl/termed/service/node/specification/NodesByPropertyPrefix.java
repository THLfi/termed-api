package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

public class NodesByPropertyPrefix implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final String value;
  private final float boost;

  public NodesByPropertyPrefix(String attributeId, String value) {
    this(attributeId, value, 1);
  }

  public NodesByPropertyPrefix(String attributeId, String value, float boost) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    this.attributeId = attributeId;
    this.value = value;
    this.boost = boost;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId).stream()
        .anyMatch(v -> v.getValue().startsWith(value));
  }

  @Override
  public Query luceneQuery() {
    return new BoostQuery(new PrefixQuery(new Term("properties." + attributeId, value)), boost);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByPropertyPrefix that = (NodesByPropertyPrefix) o;
    return Float.compare(that.boost, boost) == 0 &&
        Objects.equals(attributeId, that.attributeId) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, value, boost);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("value", value)
        .add("boost", boost)
        .toString();
  }

}
