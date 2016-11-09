package fi.thl.termed.util.xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Function;

public class JsonToXmlConverter implements Function<JsonElement, Document> {

  @Override
  public Document apply(JsonElement jsonElement) {
    Document doc = XmlUtils.newDocument();
    doc.appendChild(toElement(doc, jsonElement));
    return doc;
  }

  private Element toElement(Document doc, JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      return toElement(doc, jsonElement.getAsJsonObject());
    } else if (jsonElement.isJsonArray()) {
      return toElement(doc, jsonElement.getAsJsonArray());
    } else if (jsonElement.isJsonPrimitive()) {
      return toElement(doc, jsonElement.getAsJsonPrimitive());
    } else if (jsonElement.isJsonNull()) {
      return doc.createElement("null");
    }
    throw new IllegalStateException();
  }

  private Element toElement(Document doc, JsonArray jsonArray) {
    Element listElement = doc.createElement("array");

    for (JsonElement jsonElement : jsonArray) {
      listElement.appendChild(toElement(doc, jsonElement));
    }

    return listElement;
  }

  private Element toElement(Document doc, JsonObject jsonObject) {
    Element objectElement = doc.createElement("object");

    for (Map.Entry<String, JsonElement> jsonObjectEntry : jsonObject.entrySet()) {
      Element entryElement = (Element) objectElement.appendChild(doc.createElement("entry"));
      entryElement.setAttribute("key", jsonObjectEntry.getKey());
      entryElement.appendChild(toElement(doc, jsonObjectEntry.getValue()));
    }

    return objectElement;
  }

  private Element toElement(Document doc, JsonPrimitive jsonPrimitive) {
    if (jsonPrimitive.isString()) {
      Element stringElement = doc.createElement("string");
      stringElement.setTextContent(jsonPrimitive.getAsString());
      return stringElement;
    }
    if (jsonPrimitive.isBoolean()) {
      Element booleanElement = doc.createElement("boolean");
      booleanElement.setTextContent(jsonPrimitive.getAsString());
      return booleanElement;
    }
    if (jsonPrimitive.isNumber()) {
      Element valueElement = doc.createElement("number");
      valueElement.setTextContent(jsonPrimitive.getAsString());
      return valueElement;
    }
    throw new IllegalStateException();
  }

}
