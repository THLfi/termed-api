package fi.thl.termed.service.resource.internal;

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

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.Converter;
import fi.thl.termed.util.index.lucene.LuceneConstants;
import fi.thl.termed.util.index.lucene.LuceneException;

public class ResourceDocumentConverter extends Converter<Resource, Document> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Gson gson;

  public ResourceDocumentConverter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Document apply(Resource r) {
    Document doc = new Document();

    doc.add(new StoredField(LuceneConstants.CACHED_RESULT_FIELD,
                            CompressionTools.compressString(gson.toJson(r))));

    doc.add(stringField("type.scheme.id", r.getTypeSchemeId()));
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

    for (Map.Entry<String, ResourceId> entry : r.getReferences().entries()) {
      String property = entry.getKey();
      ResourceId value = entry.getValue();

      doc.add(stringField(property + ".resourceId", value.toString()));

      doc.add(stringField(property + ".id", value.getId()));
      doc.add(stringField(property + ".type.id", value.getTypeId()));
      doc.add(stringField(property + ".type.scheme.id", value.getTypeSchemeId()));
    }

    for (Map.Entry<String, ResourceId> entry : r.getReferrers().entries()) {
      String property = entry.getKey();
      ResourceId value = entry.getValue();

      doc.add(stringField("referrers." + property + ".resourceId", value.toString()));

      doc.add(stringField("referrers." + property + ".id", value.getId()));
      doc.add(stringField("referrers." + property + ".type.id", value.getTypeId()));
      doc.add(stringField("referrers." + property + ".type.scheme.id", value.getTypeSchemeId()));
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
  public Resource applyInverse(Document document) {
    String cachedJsonResource;

    try {
      cachedJsonResource = CompressionTools.decompressString(
          document.getBinaryValue(LuceneConstants.CACHED_RESULT_FIELD));
    } catch (DataFormatException e) {
      log.error("Failed to decompress cached value for resource {}",
                document.get(LuceneConstants.DOCUMENT_ID));
      throw new LuceneException(e);
    }

    try {
      return gson.fromJson(cachedJsonResource, Resource.class);
    } catch (JsonSyntaxException e) {
      log.error("Failed to parse {} cached json resource {}", cachedJsonResource);
      throw e;
    }
  }

}
