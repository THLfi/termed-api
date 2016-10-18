package fi.thl.termed.service.resource.internal;

import com.google.common.base.Converter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
import fi.thl.termed.util.index.lucene.LuceneConstants;
import fi.thl.termed.util.index.lucene.LuceneException;

public class ResourceDocumentConverter extends Converter<Resource, Document> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Gson gson;

  public ResourceDocumentConverter(Gson gson) {
    this.gson = gson;
  }

  @Override
  protected Document doForward(Resource r) {
    Document doc = new Document();

    Resource cachedResource = new Resource(r);
    cachedResource.setReferences(truncateValues(r.getReferences()));
    cachedResource.setReferrers(ImmutableMultimap.<String, Resource>of());
    doc.add(new StoredField(LuceneConstants.CACHED_RESULT_FIELD,
                            CompressionTools.compressString(gson.toJson(cachedResource))));

    doc.add(stringField("scheme.id", r.getSchemeId()));
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

    for (Map.Entry<String, Resource> entry : r.getReferences().entries()) {
      String property = entry.getKey();
      Resource value = entry.getValue();

      doc.add(stringField(property + ".resourceId", new ResourceId(value).toString()));

      doc.add(stringField(property + ".id", value.getId()));
      doc.add(stringField(property + ".type.id", value.getTypeId()));
      doc.add(stringField(property + ".scheme.id", value.getSchemeId()));
      doc.add(stringField(property + ".code", value.getCode()));
      doc.add(stringField(property + ".uri", value.getUri()));
    }

    for (Map.Entry<String, Resource> entry : r.getReferrers().entries()) {
      String property = entry.getKey();
      Resource value = entry.getValue();

      doc.add(stringField("referrers." + property + ".resourceId",
                          new ResourceId(value).toString()));

      doc.add(stringField("referrers." + property + ".id", value.getId()));
      doc.add(stringField("referrers." + property + ".type.id", value.getTypeId()));
      doc.add(stringField("referrers." + property + ".scheme.id", value.getSchemeId()));
      doc.add(stringField("referrers." + property + ".code", value.getCode()));
      doc.add(stringField("referrers." + property + ".uri", value.getUri()));
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

  private Multimap<String, Resource> truncateValues(Multimap<String, Resource> multimap) {
    return Multimaps.transformValues(multimap, value -> {
      Resource truncated = new Resource(new ResourceId(value));
      truncated.setCode(value.getCode());
      truncated.setUri(value.getUri());
      return truncated;
    });
  }

  @Override
  protected Resource doBackward(Document document) {
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
