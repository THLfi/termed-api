package fi.thl.termed.service.node.specification;

import static com.google.common.base.Strings.nullToEmpty;
import static org.assertj.core.util.Strings.isNullOrEmpty;

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
  private final String lang;
  private final String value;
  private final float boost;

  public NodesByPropertyPrefix(String attributeId, String value) {
    this(attributeId, "", value, 1);
  }

  public NodesByPropertyPrefix(String attributeId, String value, float boost) {
    this(attributeId, "", value, boost);
  }

  public NodesByPropertyPrefix(String attributeId, String lang, String value) {
    this(attributeId, lang, value, 1);
  }

  public NodesByPropertyPrefix(String attributeId, String lang, String value, float boost) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    Preconditions.checkArgument(isNullOrEmpty(lang) || lang.matches("[a-z]{2}"));
    this.attributeId = attributeId;
    this.lang = nullToEmpty(lang);
    this.value = value;
    this.boost = boost;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId).stream().anyMatch(
        v -> (lang.isEmpty() || v.getLang().equals(lang)) && v.getValue().startsWith(value));
  }

  @Override
  public Query luceneQuery() {
    String fieldName = "properties." + attributeId + (lang.isEmpty() ? "" : "." + lang);
    return new BoostQuery(new PrefixQuery(
        new Term(fieldName, nullToEmpty(value).toLowerCase())), boost);
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
        Objects.equals(lang, that.lang) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, lang, value, boost);
  }

}
