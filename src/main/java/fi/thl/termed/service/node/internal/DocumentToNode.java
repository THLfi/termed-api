package fi.thl.termed.service.node.internal;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.DateUtils;
import fi.thl.termed.util.UUIDs;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentToNode implements Function<Document, Node> {

  private static final Logger log = LoggerFactory.getLogger(DocumentToNode.class);

  private static final TypeAdapter<List<StrictLangValue>> propertyValuesListParser = new Gson()
      .getAdapter(new TypeToken<List<StrictLangValue>>() {
      });
  private static final Pattern commaPattern = Pattern.compile(",");

  @Override
  public Node apply(Document doc) {
    UUID id = null;
    String typeId = null;
    UUID graphId = null;

    Long number = null;
    String uri = null;
    String code = null;

    String createdBy = null;
    LocalDateTime createdDate = null;
    String lastModifiedBy = null;
    LocalDateTime lastModifiedDate = null;

    ImmutableMultimap.Builder<String, StrictLangValue> properties = ImmutableMultimap.builder();
    ImmutableMultimap.Builder<String, NodeId> references = ImmutableMultimap.builder();
    ImmutableMultimap.Builder<String, NodeId> referrers = ImmutableMultimap.builder();

    for (IndexableField field : doc.getFields()) {
      String fieldName = field.name();
      String fieldValue = field.stringValue();

      switch (fieldName) {
        case "id":
          id = UUIDs.fromString(fieldValue);
          continue;
        case "type.id":
          typeId = fieldValue;
          continue;
        case "type.graph.id":
          graphId = UUIDs.fromString(fieldValue);
          continue;
        case "number":
          number = Long.valueOf(fieldValue);
          continue;
        case "uri":
          uri = fieldValue;
          continue;
        case "code":
          code = fieldValue;
          continue;
        case "createdBy":
          createdBy = fieldValue;
          continue;
        case "createdDate":
          createdDate = stringToDate(fieldValue);
          continue;
        case "lastModifiedBy":
          lastModifiedBy = fieldValue;
          continue;
        case "lastModifiedDate":
          lastModifiedDate = stringToDate(fieldValue);
          continue;
      }

      int attrNameIndex = fieldName.lastIndexOf('.');
      int attrTypeIndex = fieldName.lastIndexOf('.', attrNameIndex - 1);

      if (attrNameIndex > 0 && attrTypeIndex > 0) {
        String attrType = fieldName.substring(attrTypeIndex + 1, attrNameIndex);
        String attrName = fieldName.substring(attrNameIndex + 1);

        switch (attrType) {
          case "properties":
            try {
              properties.putAll(attrName, propertyValuesListParser.fromJson(fieldValue));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
            continue;
          case "references":
            commaPattern.splitAsStream(fieldValue)
                .forEach(s -> references.put(attrName, NodeId.fromString(s)));
            continue;
          case "referrers":
            commaPattern.splitAsStream(fieldValue)
                .forEach(s -> referrers.put(attrName, NodeId.fromString(s)));
            continue;
          default:
            log.warn("Unexpected attrType: {}", attrType);
        }
      }
    }

    return new Node(id,
        TypeId.of(typeId, graphId),
        code,
        uri,
        number,
        createdBy,
        createdDate,
        lastModifiedBy,
        lastModifiedDate,
        properties.build(),
        references.build(),
        referrers.build());
  }

  private LocalDateTime stringToDate(String str) {
    return str != null ? DateUtils.parseLuceneDateString(str) : null;
  }

}
