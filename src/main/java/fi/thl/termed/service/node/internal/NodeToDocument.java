package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.index.lucene.LuceneConstants.MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS;
import static java.lang.Integer.min;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import java.util.Date;
import java.util.HashSet;
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

  private static final Gson gson = new Gson();

  @Override
  public Document apply(Node n) {
    Document doc = new Document();

    String qualifier = UUIDs.toString(n.getTypeGraphId()) + "." + n.getTypeId();

    doc.add(storedStringField("type.graph.id", n.getTypeGraphId()));
    doc.add(storedStringField("type.id", n.getTypeId()));
    doc.add(storedStringField("id", n.getId()));

    doc.add(storedStringField("code", n.getCode().orElse("")));
    doc.add(storedStringField("uri", n.getUri().orElse("")));
    doc.add(storedStringField("number", n.getNumber()));
    doc.add(storedStringField("createdBy", n.getCreatedBy()));
    doc.add(storedStringField("createdDate", n.getCreatedDate()));
    doc.add(storedStringField("lastModifiedBy", n.getLastModifiedBy()));
    doc.add(storedStringField("lastModifiedDate", n.getLastModifiedDate()));

    doc.add(sortableField("createdDate.sortable", n.getCreatedDate()));
    doc.add(sortableField("lastModifiedDate.sortable", n.getLastModifiedDate()));

    addProperties(doc, qualifier, n.getProperties());
    addReferences(doc, qualifier, n.getReferences());
    addReferrers(doc, qualifier, n.getReferrers());

    return doc;
  }

  private void addProperties(Document doc, String qualifier,
      Multimap<String, StrictLangValue> properties) {

    properties.asMap().forEach((property, langValues) -> {
      doc.add(storedField(qualifier + ".properties." + property, gson.toJson(langValues)));

      Set<String> sortFieldAddedForLang = new HashSet<>();
      for (StrictLangValue langValue : langValues) {
        addProperty(doc, property,
            langValue.getLang(),
            langValue.getValue(),
            sortFieldAddedForLang);
      }
    });
  }

  private void addProperty(Document doc, String property, String lang, String val,
      Set<String> sortFieldAddedForLang) {
    doc.add(textField("properties." + property, val));
    doc.add(stringField("properties." + property + ".string", val));
    if (!sortFieldAddedForLang.contains("")) {
      doc.add(sortableField("properties." + property + ".sortable", val.toLowerCase()));
      sortFieldAddedForLang.add("");
    }

    if (!lang.isEmpty()) {
      doc.add(textField("properties." + property + "." + lang, val));
      doc.add(stringField("properties." + property + "." + lang + ".string", val));
      if (!sortFieldAddedForLang.contains(lang)) {
        doc.add(
            sortableField("properties." + property + "." + lang + ".sortable", val.toLowerCase()));
        sortFieldAddedForLang.add(lang);
      }
    }
  }

  private void addReferences(Document doc, String qualifier, Multimap<String, NodeId> references) {
    references.asMap().forEach((property, values) -> {
      doc.add(storedField(qualifier + ".references." + property,
          values.stream().map(NodeId::toString).collect(joining(","))));

      values.forEach(value -> {
        doc.add(stringField("references.nodeId", value.toString()));
        doc.add(stringField("references." + property + ".nodeId", value.toString()));

        doc.add(stringField("references." + property + ".id", value.getId()));
        doc.add(stringField("references." + property + ".type.id", value.getTypeId()));
        doc.add(stringField("references." + property + ".type.graph.id", value.getTypeGraphId()));
      });
    });
  }

  private void addReferrers(Document doc, String qualifier, Multimap<String, NodeId> referrers) {
    referrers.asMap().forEach((property, values) -> {
      doc.add(storedField(qualifier + ".referrers." + property,
          values.stream().map(NodeId::toString).collect(joining(","))));

      values.forEach(value -> {
        doc.add(stringField("referrers.nodeId", value.toString()));
        doc.add(stringField("referrers." + property + ".nodeId", value.toString()));

        doc.add(stringField("referrers." + property + ".id", value.getId()));
        doc.add(stringField("referrers." + property + ".type.id", value.getTypeId()));
        doc.add(stringField("referrers." + property + ".type.graph.id", value.getTypeGraphId()));
      });
    });
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

  private Field storedStringField(String name, String value) {
    return new StringField(name,
        value.substring(0, min(MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS, value.length())),
        Store.YES);
  }

  private Field storedStringField(String name, UUID value) {
    return new StringField(name, UUIDs.toString(value), Store.YES);
  }

  private Field storedStringField(String name, Date value) {
    return new StringField(name, DateTools.dateToString(value, Resolution.MILLISECOND), Store.YES);
  }

  private Field storedStringField(String name, Long value) {
    return new StringField(name, String.valueOf(value), Store.YES);
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

  private Field storedField(String name, String value) {
    return new StoredField(name, value);
  }

}
