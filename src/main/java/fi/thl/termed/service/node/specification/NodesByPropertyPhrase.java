package fi.thl.termed.service.node.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.specification.LuceneSpecification;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

public class NodesByPropertyPhrase implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final List<String> values;

  public NodesByPropertyPhrase(String attributeId, List<String> values) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    this.attributeId = attributeId;
    this.values = values;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId).stream()
        .anyMatch(v -> v.getValue().equals(String.join(" ", values)));
  }

  @Override
  public Query luceneQuery() {
    PhraseQuery phraseQuery = new PhraseQuery();
    for (String value : values) {
      phraseQuery.add(new Term("properties." + attributeId, value));
    }
    return phraseQuery;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByPropertyPhrase that = (NodesByPropertyPhrase) o;
    return Objects.equals(attributeId, that.attributeId) &&
        Objects.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, values);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("values", values)
        .toString();
  }

}
