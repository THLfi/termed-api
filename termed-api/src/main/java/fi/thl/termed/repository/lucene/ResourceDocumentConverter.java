package fi.thl.termed.repository.lucene;

import com.google.common.base.Converter;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.zip.DataFormatException;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.util.LangValue;

import static fi.thl.termed.repository.lucene.LuceneConstants.DEFAULT_SEARCH_FIELD;

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
    cachedResource.setReferences(truncateReferences(r.getReferences()));
    cachedResource.setReferrers(ImmutableMultimap.<String, Resource>of());
    doc.add(new StoredField(LuceneConstants.CACHED_RESULT_FIELD,
                            CompressionTools.compressString(gson.toJson(cachedResource))));

    doc.add(new LowerCaseCodeField("id", r.getId()));
    doc.add(new LowerCaseCodeField("type.id", r.getTypeId()));
    doc.add(new LowerCaseCodeField("scheme.id", r.getSchemeId()));
    doc.add(new LowerCaseCodeField("code", r.getCode()));
    doc.add(new LowerCaseCodeField("uri", r.getUri()));

    for (Map.Entry<String, LangValue> entry : r.getProperties().entries()) {
      String property = entry.getKey();
      String lang = entry.getValue().getLang();
      String value = entry.getValue().getValue();

      doc.add(new TextField(property, value, Store.NO));
      doc.add(new TextField(DEFAULT_SEARCH_FIELD, value, Store.NO));
      doc.add(new StringField(property + ".sortable", value, Store.NO));

      if (!lang.isEmpty()) {
        doc.add(new TextField(property + "." + lang, value, Store.NO));
        doc.add(new StringField(property + "." + lang + ".sortable", value, Store.NO));
      }
    }

    for (Map.Entry<String, Resource> entry : r.getReferences().entries()) {
      String property = entry.getKey();
      Resource value = entry.getValue();

      doc.add(new LowerCaseCodeField(property + ".id", value.getId()));
      doc.add(new LowerCaseCodeField(property + ".type.id", value.getTypeId()));
      doc.add(new LowerCaseCodeField(property + ".scheme.id", value.getSchemeId()));
      doc.add(new LowerCaseCodeField(property + ".code", value.getCode()));
      doc.add(new LowerCaseCodeField(property + ".uri", value.getUri()));
    }

    for (Map.Entry<String, Resource> entry : r.getReferrers().entries()) {
      String property = entry.getKey();
      Resource value = entry.getValue();

      doc.add(new LowerCaseCodeField("referrers." + property + ".id", value.getId()));
      doc.add(new LowerCaseCodeField("referrers." + property + ".type.id", value.getTypeId()));
      doc.add(new LowerCaseCodeField("referrers." + property + ".scheme.id", value.getSchemeId()));
      doc.add(new LowerCaseCodeField("referrers." + property + ".code", value.getCode()));
      doc.add(new LowerCaseCodeField("referrers." + property + ".uri", value.getUri()));
    }

    return doc;
  }

  private Multimap<String, Resource> truncateReferences(Multimap<String, Resource> references) {
    Multimap<String, Resource> refs = MultimapBuilder.linkedHashKeys().arrayListValues().build();

    for (Map.Entry<String, Resource> entry : references.entries()) {
      Resource value = entry.getValue();
      Resource truncatedValue = new Resource();

      truncatedValue.setScheme(new Scheme(value.getSchemeId()));
      truncatedValue.setType(new Class(value.getTypeId()));
      truncatedValue.setId(value.getId());

      refs.put(entry.getKey(), truncatedValue);
    }

    return refs;
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
