package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_REFERRERS_FIELD;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_RESULT_FIELD;
import static fi.thl.termed.util.index.lucene.LuceneConstants.MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS;
import static java.lang.Integer.min;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;

public class NodeToDocument implements Function<Node, Document> {

  private Gson gson;

  public NodeToDocument(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Document apply(Node n) {
    Document doc = new Document();

    Node.Builder cachedNodeBuilder = Node.builderFromCopyOf(n);
    cachedNodeBuilder.referrers(null);
    doc.add(new StoredField(CACHED_RESULT_FIELD, gson.toJson(cachedNodeBuilder.build())));
    doc.add(new StoredField(CACHED_REFERRERS_FIELD, gson.toJson(n.getReferrers())));

    doc.add(stringField("type.graph.id", n.getTypeGraphId()));
    doc.add(stringField("type.id", n.getTypeId()));
    doc.add(stringField("id", n.getId()));
    doc.add(stringField("code", n.getCode().orElse("")));
    doc.add(stringField("uri", n.getUri().orElse("")));
    doc.add(stringField("number", n.getNumber()));

    doc.add(stringField("createdDate", n.getCreatedDate()));
    doc.add(stringField("lastModifiedDate", n.getLastModifiedDate()));
    doc.add(sortableField("createdDate.sortable", n.getCreatedDate()));
    doc.add(sortableField("lastModifiedDate.sortable", n.getLastModifiedDate()));

    addProperties(doc, n.getProperties());
    addReferences(doc, n.getReferences());
    addReferrers(doc, n.getReferrers());

    return doc;
  }

  private void addProperties(Document doc, Multimap<String, StrictLangValue> properties) {
    properties.asMap().forEach((p, langValues) -> {
      boolean sortFieldAdded = false;
      Set<String> sortFieldAddedForLang = new HashSet<>();

      for (StrictLangValue langValue : langValues) {
        String lang = langValue.getLang();
        String val = langValue.getValue();

        doc.add(textField("properties." + p, val));
        doc.add(stringField("properties." + p + ".string", val));

        if (!sortFieldAdded) {
          doc.add(sortableField("properties." + p + ".sortable", val.toLowerCase()));
          sortFieldAdded = true;
        }

        if (!lang.isEmpty()) {
          doc.add(textField("properties." + p + "." + lang, val));
          doc.add(stringField("properties." + p + "." + lang + ".string", val));

          if (!sortFieldAddedForLang.contains(lang)) {
            doc.add(sortableField("properties." + p + "." + lang + ".sortable", val.toLowerCase()));
            sortFieldAddedForLang.add(lang);
          }
        }
      }
    });
  }

  private void addReferences(Document doc, Multimap<String, NodeId> references) {
    for (Map.Entry<String, NodeId> entry : references.entries()) {
      String property = entry.getKey();
      NodeId value = entry.getValue();

      doc.add(stringField("references.nodeId", value.toString()));
      doc.add(stringField("references." + property + ".nodeId", value.toString()));

      doc.add(stringField("references." + property + ".id", value.getId()));
      doc.add(stringField("references." + property + ".type.id", value.getTypeId()));
      doc.add(stringField("references." + property + ".type.graph.id", value.getTypeGraphId()));
    }
  }

  private void addReferrers(Document doc, Multimap<String, NodeId> referrers) {
    for (Map.Entry<String, NodeId> entry : referrers.entries()) {
      String property = entry.getKey();
      NodeId value = entry.getValue();

      doc.add(stringField("referrers.nodeId", value.toString()));
      doc.add(stringField("referrers." + property + ".nodeId", value.toString()));

      doc.add(stringField("referrers." + property + ".id", value.getId()));
      doc.add(stringField("referrers." + property + ".type.id", value.getTypeId()));
      doc.add(stringField("referrers." + property + ".type.graph.id", value.getTypeGraphId()));
    }
  }

  private Field textField(String name, String value) {
    return new TextField(name, value, Store.NO);
  }

  private Field stringField(String name, String value) {
    return new StringField(name,
        value.substring(0, min(MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS, value.length())),
        Store.NO);
  }

  private Field stringField(String name, UUID value) {
    return new StringField(name, UUIDs.toString(value), Store.NO);
  }

  private Field stringField(String name, Date value) {
    return new StringField(name, DateTools.dateToString(value, Resolution.MILLISECOND), Store.NO);
  }

  private Field stringField(String name, Long value) {
    return new StringField(name, String.valueOf(value), Store.NO);
  }

  private Field sortableField(String name, String value) {
    return new SortedDocValuesField(name,
        new BytesRef(
            value.substring(0, min(MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS, value.length()))));
  }

  private Field sortableField(String name, Date value) {
    return new SortedDocValuesField(name,
        new BytesRef(DateTools.dateToString(value, Resolution.MILLISECOND)));
  }

}
