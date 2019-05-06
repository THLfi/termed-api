package fi.thl.termed.util.index.lucene;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fi.thl.termed.util.Converter;
import fi.thl.termed.util.json.JsonUtils;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDocumentConverter<V> extends Converter<V, Document> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Gson gson;

  private Class<V> type;

  public JsonDocumentConverter(Gson gson, Class<V> type) {
    this.gson = gson;
    this.type = type;
  }

  @Override
  public Document apply(V v) {
    Document doc = new Document();

    JsonUtils.flatten(gson.toJsonTree(v)).forEach((field, value) -> {
      // will be used in backward transformation
      doc.add(new StoredField(field, value));
      // index without array indices
      doc.add(new TextField(field.replaceAll("\\[\\d+\\]", ""), value, Field.Store.NO));
      // kitchen sink field for easy searching
      doc.add(new TextField(LuceneConstants.DEFAULT_SEARCH_FIELD, value, Field.Store.NO));
    });

    return doc;
  }

  @Override
  public V applyInverse(Document document) {
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
