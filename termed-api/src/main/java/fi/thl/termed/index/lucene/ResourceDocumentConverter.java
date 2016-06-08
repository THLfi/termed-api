package fi.thl.termed.index.lucene;

import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.StrictLangValue;

import static fi.thl.termed.index.lucene.LuceneConstants.DEFAULT_SEARCH_FIELD;

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

    doc.add(new LowerCaseCodeField("id", r.getId()));
    doc.add(new LowerCaseCodeField(DEFAULT_SEARCH_FIELD, r.getId()));

    doc.add(new LowerCaseCodeField("type.id", r.getTypeId()));
    doc.add(new LowerCaseCodeField(DEFAULT_SEARCH_FIELD, r.getTypeId()));

    doc.add(new LowerCaseCodeField("scheme.id", r.getSchemeId()));
    doc.add(new LowerCaseCodeField(DEFAULT_SEARCH_FIELD, r.getSchemeId()));

    doc.add(new LowerCaseCodeField("code", r.getCode()));
    doc.add(new LowerCaseCodeField(DEFAULT_SEARCH_FIELD, r.getCode()));

    doc.add(new LowerCaseCodeField("uri", r.getUri()));
    doc.add(new LowerCaseCodeField(DEFAULT_SEARCH_FIELD, r.getUri()));

    for (Map.Entry<String, StrictLangValue> entry : r.getProperties().entries()) {
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

  private Multimap<String, Resource> truncateValues(Multimap<String, Resource> multimap) {
    return Multimaps.transformValues(multimap, new Function<Resource, Resource>() {
      public Resource apply(Resource value) {
        Resource truncated = new Resource(new ResourceId(value));
        truncated.setCode(value.getCode());
        truncated.setUri(value.getUri());
        return truncated;
      }
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
