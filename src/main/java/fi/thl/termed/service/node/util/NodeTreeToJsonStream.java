package fi.thl.termed.service.node.util;

import com.google.common.collect.Multimap;
import com.google.gson.stream.JsonWriter;
import fi.thl.termed.domain.NodeTree;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public final class NodeTreeToJsonStream {

  private NodeTreeToJsonStream() {
  }

  /**
   * Writes given node trees to given JSON writer. Leaves the JSON writer open.
   */
  public static void toJson(Iterator<NodeTree> iterator, JsonWriter writer) throws IOException {
    writer.beginArray();
    while (iterator.hasNext()) {
      NodeTreeToJsonStream.toJson(iterator.next(), writer);
    }
    writer.endArray();
  }

  /**
   * Writes given node tree to given JSON writer. Leaves the JSON writer open.
   */
  public static void toJson(NodeTree tree, JsonWriter writer) throws IOException {
    writer.beginObject();

    if (tree.getId() != null) {
      writer.name("id");
      writer.value(UUIDs.toString(tree.getId()));
    }
    if (tree.getCode().isPresent()) {
      writer.name("code");
      writer.value(tree.getCode().get());
    }
    if (tree.getUri().isPresent()) {
      writer.name("uri");
      writer.value(tree.getUri().get());
    }
    if (tree.getNumber() != null) {
      writer.name("number");
      writer.value(tree.getNumber());
    }

    if (tree.getCreatedBy() != null) {
      writer.name("createdBy");
      writer.value(tree.getCreatedBy());
    }
    if (tree.getCreatedDate() != null) {
      writer.name("createdDate");
      writer.value(tree.getCreatedDate()
          .atZone(ZoneId.systemDefault())
          .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
    if (tree.getLastModifiedBy() != null) {
      writer.name("lastModifiedBy");
      writer.value(tree.getLastModifiedBy());
    }
    if (tree.getLastModifiedDate() != null) {
      writer.name("lastModifiedDate");
      writer.value(tree.getLastModifiedDate()
          .atZone(ZoneId.systemDefault())
          .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    if (tree.getType() != null) {
      writer.name("type");
      writer.beginObject();
      writer.name("id");
      writer.value(tree.getType().getId());
      writer.name("graph");
      writer.beginObject();
      writer.name("id");
      writer.value(UUIDs.toString(tree.getType().getGraphId()));
      writer.endObject();
      writer.endObject();
    }

    writeProperties(tree.getProperties(), writer);
    writeReferences(tree.getReferences(), writer);
    writeReferrers(tree.getReferrers(), writer);

    writer.endObject();
  }

  private static void writeProperties(Multimap<String, StrictLangValue> properties,
      JsonWriter writer) throws IOException {
    if (properties != null) {
      writer.name("properties");
      writer.beginObject();
      for (String key : properties.keySet()) {
        writer.name(key);
        writer.beginArray();
        for (StrictLangValue value : properties.get(key)) {
          writer.beginObject();
          writer.name("lang");
          writer.value(value.getLang());
          writer.name("value");
          writer.value(value.getValue());
          writer.name("regex");
          writer.value(value.getRegex());
          writer.endObject();
        }
        writer.endArray();
      }
      writer.endObject();
    }
  }

  private static void writeReferences(Multimap<String, ? extends NodeTree> references,
      JsonWriter writer) throws IOException {
    if (references != null) {
      writer.name("references");
      writer.beginObject();
      for (String key : references.keySet()) {
        writer.name(key);
        writer.beginArray();
        for (NodeTree value : references.get(key)) {
          toJson(value, writer);
        }
        writer.endArray();
      }
      writer.endObject();
    }
  }

  private static void writeReferrers(Multimap<String, ? extends NodeTree> referrers,
      JsonWriter writer) throws IOException {
    if (referrers != null) {
      writer.name("referrers");
      writer.beginObject();
      for (String key : referrers.keySet()) {
        writer.name(key);
        writer.beginArray();
        for (NodeTree value : referrers.get(key)) {
          toJson(value, writer);
        }
        writer.endArray();
      }
      writer.endObject();
    }
  }

}
