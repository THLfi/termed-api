package fi.thl.termed.service.node.util;

import com.google.gson.stream.JsonWriter;
import fi.thl.termed.domain.NodeTree;
import fi.thl.termed.domain.StrictLangValue;
import java.io.IOException;
import java.util.Iterator;
import org.joda.time.DateTime;

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
      writer.value(tree.getId().toString());
    }
    if (tree.getCode() != null) {
      writer.name("code");
      writer.value(tree.getCode());
    }
    if (tree.getUri() != null) {
      writer.name("uri");
      writer.value(tree.getUri());
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
      writer.value(new DateTime(tree.getCreatedDate()).toString());
    }
    if (tree.getLastModifiedBy() != null) {
      writer.name("lastModifiedBy");
      writer.value(tree.getLastModifiedBy());
    }
    if (tree.getLastModifiedDate() != null) {
      writer.name("lastModifiedDate");
      writer.value(new DateTime(tree.getLastModifiedDate()).toString());
    }

    if (tree.getType() != null) {
      writer.name("type");
      writer.beginObject();
      writer.name("id");
      writer.value(tree.getType().getId());
      writer.name("graph");
      writer.beginObject();
      writer.name("id");
      writer.value(tree.getType().getGraphId().toString());
      writer.endObject();
      writer.endObject();
    }

    if (tree.getProperties() != null) {
      writer.name("properties");
      writer.beginObject();

      for (String key : tree.getProperties().keySet()) {
        writer.name(key);
        writer.beginArray();
        for (StrictLangValue value : tree.getProperties().get(key)) {
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

    if (tree.getReferences() != null) {
      writer.name("references");
      writer.beginObject();

      for (String key : tree.getReferences().keySet()) {
        writer.name(key);
        writer.beginArray();
        for (NodeTree value : tree.getReferences().get(key)) {
          toJson(value, writer);
        }
        writer.endArray();
      }

      writer.endObject();
    }

    if (tree.getReferrers() != null) {
      writer.name("referrers");
      writer.beginObject();

      for (String key : tree.getReferrers().keySet()) {
        writer.name(key);
        writer.beginArray();
        for (NodeTree value : tree.getReferrers().get(key)) {
          toJson(value, writer);
        }
        writer.endArray();
      }

      writer.endObject();
    }

    writer.endObject();
  }

}
