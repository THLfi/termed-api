package fi.thl.termed.service.node.internal;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.Converter;
import fi.thl.termed.util.index.lucene.LuceneConstants;
import fi.thl.termed.util.index.lucene.LuceneException;

public class NodeDocumentConverter extends Converter<Node, Document> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Gson gson;

  public NodeDocumentConverter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Document apply(Node r) {
    Document doc = new Document();

    doc.add(new StoredField(LuceneConstants.CACHED_RESULT_FIELD,
                            CompressionTools.compressString(gson.toJson(r))));

    doc.add(stringField("type.graph.id", r.getTypeGraphId()));
    doc.add(stringField("type.id", r.getTypeId()));
    doc.add(stringField("id", r.getId()));
    doc.add(stringField("code", r.getCode()));
    doc.add(stringField("uri", r.getUri()));

    for (Map.Entry<String, StrictLangValue> entry : r.getProperties().entries()) {
      String property = entry.getKey();
      String lang = entry.getValue().getLang();
      String value = entry.getValue().getValue();

      doc.add(textField(property, value));
      doc.add(stringField(property + ".sortable", value));

      if (!lang.isEmpty()) {
        doc.add(textField(property + "." + lang, value));
        doc.add(stringField(property + "." + lang + ".sortable", value));
      }
    }

    for (Map.Entry<String, NodeId> entry : r.getReferences().entries()) {
      String property = entry.getKey();
      NodeId value = entry.getValue();

      doc.add(stringField(property + ".nodeId", value.toString()));

      doc.add(stringField(property + ".id", value.getId()));
      doc.add(stringField(property + ".type.id", value.getTypeId()));
      doc.add(stringField(property + ".type.graph.id", value.getTypeGraphId()));
    }

    for (Map.Entry<String, NodeId> entry : r.getReferrers().entries()) {
      String property = entry.getKey();
      NodeId value = entry.getValue();

      doc.add(stringField("referrers." + property + ".nodeId", value.toString()));

      doc.add(stringField("referrers." + property + ".id", value.getId()));
      doc.add(stringField("referrers." + property + ".type.id", value.getTypeId()));
      doc.add(stringField("referrers." + property + ".type.graph.id", value.getTypeGraphId()));
    }

    return doc;
  }

  private Field textField(String name, String value) {
    return new TextField(name, Strings.nullToEmpty(value), Field.Store.NO);
  }

  private Field stringField(String name, String value) {
    return new StringField(name, Strings.nullToEmpty(value), Field.Store.NO);
  }

  private Field stringField(String name, UUID value) {
    return new StringField(name, value != null ? value.toString() : "", Field.Store.NO);
  }

  @Override
  public Node applyInverse(Document document) {
    String cachedJsonNode;

    try {
      cachedJsonNode = CompressionTools.decompressString(
          document.getBinaryValue(LuceneConstants.CACHED_RESULT_FIELD));
    } catch (DataFormatException e) {
      log.error("Failed to decompress cached value for node {}",
                document.get(LuceneConstants.DOCUMENT_ID));
      throw new LuceneException(e);
    }

    try {
      return gson.fromJson(cachedJsonNode, Node.class);
    } catch (JsonSyntaxException e) {
      log.error("Failed to parse {} cached json node {}", cachedJsonNode);
      throw e;
    }
  }

}