package fi.thl.termed.service.node.internal;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentToNode implements Function<Document, Node> {

  private static final Gson gson = new Gson();
  private static final TypeToken<List<StrictLangValue>> propertyValuesTypeToken =
      new TypeToken<List<StrictLangValue>>() {
      };
  private static final Logger log = LoggerFactory.getLogger(DocumentToNode.class);

  @Override
  public Node apply(Document doc) {
    Map<String, String> fields = doc.getFields().stream()
        .collect(toMap(IndexableField::name, IndexableField::stringValue, (l, r) -> {
          log.warn("Unexpected duplicate: {} for: {}", l, r);
          return r;
        }));

    String typeId = fields.get("type.id");
    // 38 = 36 char UUID and 2 dot chars
    int qualifierLength = 38 + typeId.length();

    Node.Builder builder = Node.builder().id(
        UUIDs.fromString(fields.get("id")),
        typeId,
        UUIDs.fromString(fields.get("type.graph.id")));

    fields.forEach((name, value) -> {
      String fieldName = name.length() > qualifierLength ? name.substring(qualifierLength) : name;

      if (fieldName.equals("number")) {
        builder.number(Long.valueOf(value));
      } else if (fieldName.equals("uri")) {
        builder.uri(value);
      } else if (fieldName.equals("code")) {
        builder.code(value);
      } else if (fieldName.equals("createdBy")) {
        builder.createdBy(value);
      } else if (fieldName.equals("createdDate")) {
        builder.createdDate(stringToDate(value));
      } else if (fieldName.equals("lastModifiedBy")) {
        builder.lastModifiedBy(value);
      } else if (fieldName.equals("lastModifiedDate")) {
        builder.lastModifiedDate(stringToDate(value));
      } else if (fieldName.startsWith("properties.")) {
        String property = name.substring(qualifierLength + 11); // "properties.".length() == 11
        List<StrictLangValue> values = gson.fromJson(value, propertyValuesTypeToken.getType());
        builder.addProperty(property, values);
      } else if (fieldName.startsWith("references.")) {
        String property = name.substring(qualifierLength + 11); // "references.".length() == 11
        List<NodeId> values = Stream.of(value.split(","))
            .map(NodeId::fromString)
            .collect(toList());
        builder.addReference(property, values);
      } else if (fieldName.startsWith("referrers.")) {
        String property = name.substring(qualifierLength + 10); // "referrers.".length() == 10
        List<NodeId> values = Stream.of(value.split(","))
            .map(NodeId::fromString)
            .collect(toList());
        builder.addReferrer(property, values);
      }
    });

    return builder.build();
  }

  private Date stringToDate(String str) {
    try {
      return str != null ? DateTools.stringToDate(str) : null;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

}
