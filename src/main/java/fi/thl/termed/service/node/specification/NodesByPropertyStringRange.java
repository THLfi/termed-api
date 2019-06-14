package fi.thl.termed.service.node.specification;

import static com.google.common.base.Strings.nullToEmpty;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.IETF_LANGUAGE_TAG;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class NodesByPropertyStringRange implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final String lang;
  private final String lower;
  private final String upper;

  public NodesByPropertyStringRange(String attributeId, String lang, String lower, String upper) {
    Preconditions.checkArgument(attributeId.matches(CODE));
    Preconditions.checkArgument(isNullOrEmpty(lang) || lang.matches(IETF_LANGUAGE_TAG));
    this.attributeId = attributeId;
    this.lang = nullToEmpty(lang);
    this.lower = lower;
    this.upper = upper;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId)
        .stream()
        .filter(v -> lang.isEmpty() || v.getLang().equals(lang))
        .map(StrictLangValue::getValue)
        .anyMatch(v -> (lower == null || lower.compareTo(v) <= 0) &&
            (upper == null || upper.compareTo(v) >= 0));
  }

  @Override
  public Query luceneQuery() {
    String fieldName = "properties." + attributeId + (lang.isEmpty() ? "" : "." + lang) + ".string";

    return new TermRangeQuery(
        fieldName,
        lower != null ? new BytesRef(lower) : null,
        upper != null ? new BytesRef(upper) : null,
        true,
        true);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodesByPropertyStringRange that = (NodesByPropertyStringRange) o;
    return Objects.equals(attributeId, that.attributeId) &&
        Objects.equals(lang, that.lang) &&
        Objects.equals(lower, that.lower) &&
        Objects.equals(upper, that.upper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, lang, lower, upper);
  }

  @Override
  public String toString() {
    return "properties." + attributeId + (lang.isEmpty() ? "" : "." + lang) + ".string = [" +
        (lower != null ? lower : "*") + " TO " +
        (upper != null ? upper : "*") + "]";
  }

}
