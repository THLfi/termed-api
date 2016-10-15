package fi.thl.termed.util.index.lucene;

import com.google.common.base.Converter;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import fi.thl.termed.util.json.JsonUtils;

public class DocumentJsonConverter<V> extends Converter<V, Document> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Gson gson;

  private Class<V> type;

  public DocumentJsonConverter(Gson gson, Class<V> type) {
    this.gson = gson;
    this.type = type;
  }

  @Override
  protected Document doForward(V v) {
    Document doc = new Document();

    for (Map.Entry<String, String> entry : JsonUtils.flatten(gson.toJsonTree(v)).entrySet()) {
      String field = entry.getKey();
      String value = entry.getValue();

      // will be used in backward transformation, useful also for sorting
      doc.add(new StringField(field, value, Field.Store.YES));
      // index also without array indices
      doc.add(new TextField(field.replaceAll("\\[\\d+\\]", ""), value, Field.Store.NO));
      // kitchen sink field for easy searching
      doc.add(new TextField(LuceneConstants.DEFAULT_SEARCH_FIELD, value, Field.Store.NO));
    }

    return doc;
  }

  @Override
  protected V doBackward(Document document) {
    Map<String, String> map = Maps.newLinkedHashMap();

    for (IndexableField field : document.getFields()) {
      map.put(field.name(), field.stringValue());
    }

    try {
      return gson.fromJson(JsonUtils.unflatten(map), type);
    } catch (JsonSyntaxException e) {
      log.error("failed to parse {} into {}", map, type);
      throw e;
    }
  }

}
