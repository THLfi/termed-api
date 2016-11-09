package fi.thl.termed.util.xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.function.Function;

public class XmlToJsonConverter implements Function<Document, JsonElement> {

  @Override
  public JsonElement apply(Document doc) {
    return toJsonElement(doc.getDocumentElement());
  }

  private JsonElement toJsonElement(Element element) {
    switch (element.getNodeName()) {
      case "array":
        return toJsonArray(element);
      case "object":
        return toJsonObject(element);
      case "string":
        return toJsonStringPrimitive(element);
      case "boolean":
        return toJsonBooleanPrimitive(element);
      case "number":
        return toJsonNumberPrimitive(element);
      case "null":
        return JsonNull.INSTANCE;
      default:
        throw new IllegalStateException();
    }
  }

  private JsonArray toJsonArray(Element element) {
    JsonArray jsonArray = new JsonArray();

    NodeList children = element.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      Node childNode = children.item(i);

      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        jsonArray.add(toJsonElement((Element) childNode));
      }
    }

    return jsonArray;
  }

  private JsonObject toJsonObject(Element element) {
    JsonObject jsonObject = new JsonObject();

    NodeList entries = element.getChildNodes();

    for (int i = 0; i < entries.getLength(); i++) {
      Node entry = entries.item(i);

      if (entry.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) entry;
        String key = childElement.getAttribute("key");

        NodeList values = childElement.getChildNodes();

        for (int j = 0; j < values.getLength(); j++) {
          Node value = values.item(j);
          if (value.getNodeType() == Node.ELEMENT_NODE) {
            jsonObject.add(key, toJsonElement((Element) value));
          }
        }
      }
    }

    return jsonObject;
  }

  private JsonPrimitive toJsonStringPrimitive(Element element) {
    return new JsonPrimitive(element.getTextContent());
  }

  private JsonPrimitive toJsonBooleanPrimitive(Element element) {
    return new JsonPrimitive(Boolean.valueOf(element.getTextContent()));
  }

  private JsonPrimitive toJsonNumberPrimitive(Element element) {
    try {
      return new JsonPrimitive(Integer.parseInt(element.getTextContent()));
    } catch (NumberFormatException e) {
      return new JsonPrimitive(Double.parseDouble(element.getTextContent()));
    }
  }

}
