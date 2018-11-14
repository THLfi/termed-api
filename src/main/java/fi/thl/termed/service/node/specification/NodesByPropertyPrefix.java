package fi.thl.termed.service.node.specification;

import static com.google.common.base.Strings.nullToEmpty;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.IETF_LANGUAGE_TAG;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

public class NodesByPropertyPrefix implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final String lang;
  private final String value;

  public NodesByPropertyPrefix(String attributeId, String value) {
    this(attributeId, "", value);
  }

  public NodesByPropertyPrefix(String attributeId, String lang, String value) {
    Preconditions.checkArgument(attributeId.matches(CODE));
    Preconditions.checkArgument(isNullOrEmpty(lang) || lang.matches(IETF_LANGUAGE_TAG));
    this.attributeId = attributeId;
    this.lang = nullToEmpty(lang);
    this.value = value;
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
        .flatMap(v -> Stream.of(v.getValue().split("\\s")))
        .anyMatch(v -> v.toLowerCase().startsWith(value.toLowerCase()));
  }

  @Override
  public Query luceneQuery() {
    String fieldName = "properties." + attributeId + (lang.isEmpty() ? "" : "." + lang);
    return new PrefixQuery(new Term(fieldName, nullToEmpty(value).toLowerCase()));
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
    return Objects.equals(attributeId, that.attributeId) &&
        Objects.equals(lang, that.lang) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, lang, value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("lang", lang)
        .add("value", value)
        .toString();
  }

}
