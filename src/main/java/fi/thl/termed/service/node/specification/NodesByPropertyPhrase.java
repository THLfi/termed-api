package fi.thl.termed.service.node.specification;

import static com.google.common.base.Strings.nullToEmpty;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.query.LuceneSpecification;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

public class NodesByPropertyPhrase implements LuceneSpecification<NodeId, Node> {

  private final String attributeId;
  private final String lang;
  private final String phrase;

  public NodesByPropertyPhrase(String attributeId, String lang, String phrase) {
    Preconditions.checkArgument(attributeId.matches(RegularExpressions.CODE));
    Preconditions.checkArgument(isNullOrEmpty(lang) || lang.matches("[a-z]{2}"));
    this.attributeId = attributeId;
    this.lang = nullToEmpty(lang);
    this.phrase = phrase;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    return node.getProperties().get(attributeId).stream()
        .anyMatch(v -> (lang.isEmpty() || v.getLang().equals(lang)) && v.getValue().equals(phrase));
  }

  @Override
  public Query luceneQuery() {
    String fieldName = "properties." + attributeId + (lang.isEmpty() ? "" : "." + lang);

    PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
    for (String value : phrase.split("\\s")) {
      phraseQuery.add(new Term(fieldName, nullToEmpty(value).toLowerCase()));
    }
    return phraseQuery.build();
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
        Objects.equals(lang, that.lang) &&
        Objects.equals(phrase, that.phrase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId, lang, phrase);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .add("lang", lang)
        .add("phrase", phrase)
        .toString();
  }
  
}
