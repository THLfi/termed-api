package fi.thl.termed.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.w3c.dom.Document;

import fi.thl.termed.util.xml.JsonToXmlConverter;
import fi.thl.termed.util.xml.XmlToJsonConverter;
import fi.thl.termed.util.xml.XmlUtils;

import static org.junit.Assert.assertEquals;

public class XmlToJsonConverterTest {

  private String exampleJson =
      "{\n"
      + "  \"name\": \"Tim\",\n"
      + "  \"age\": 33,\n"
      + "  \"gender\": null,\n"
      + "  \"address\": {\n"
      + "    \"street\": \"Example Street 1\",\n"
      + "    \"zip\": \"000123\"\n"
      + "  },\n"
      + "  \"data\": [\n"
      + "    [\n"
      + "      0,\n"
      + "      1.12,\n"
      + "      null\n"
      + "    ],\n"
      + "    [\n"
      + "      2,\n"
      + "      3\n"
      + "    ],\n"
      + "    [\n"
      + "      true,\n"
      + "      false\n"
      + "    ]\n"
      + "  ]\n"
      + "}";

  private String exampleXml =
      "<object>\n"
      + "  <entry key=\"name\">\n"
      + "    <string>Tim</string>\n"
      + "  </entry>\n"
      + "  <entry key=\"age\">\n"
      + "    <number>33</number>\n"
      + "  </entry>\n"
      + "  <entry key=\"gender\">\n"
      + "    <null/>\n"
      + "  </entry>\n"
      + "  <entry key=\"address\">\n"
      + "    <object>\n"
      + "      <entry key=\"street\">\n"
      + "        <string>Example Street 1</string>\n"
      + "      </entry>\n"
      + "      <entry key=\"zip\">\n"
      + "        <string>000123</string>\n"
      + "      </entry>\n"
      + "    </object>\n"
      + "  </entry>\n"
      + "  <entry key=\"data\">\n"
      + "    <array>\n"
      + "      <array>\n"
      + "        <number>0</number>\n"
      + "        <number>1.12</number>\n"
      + "        <null/>\n"
      + "      </array>\n"
      + "      <array>\n"
      + "        <number>2</number>\n"
      + "        <number>3</number>\n"
      + "      </array>\n"
      + "      <array>\n"
      + "        <boolean>true</boolean>\n"
      + "        <boolean>false</boolean>\n"
      + "      </array>\n"
      + "    </array>\n"
      + "  </entry>\n"
      + "</object>\n";

  @Test
  public void shouldTransformJsonToXml() {
    JsonToXmlConverter toXmlConverter = new JsonToXmlConverter();
    JsonElement jsonElement = new JsonParser().parse(exampleJson);

    Document expected = XmlUtils.parseDocument(exampleXml);

    assertEquals(XmlUtils.prettyPrint(expected),
                 XmlUtils.prettyPrint(toXmlConverter.apply(jsonElement)));
  }

  @Test
  public void shouldTransformXmlToJson() {
    XmlToJsonConverter toJsonConverter = new XmlToJsonConverter();
    JsonElement jsonElement = toJsonConverter.apply(XmlUtils.parseDocument(exampleXml));
    assertEquals(new JsonParser().parse(exampleJson), jsonElement);
  }

  @Test
  public void shouldTransformJsonToXmlAndBack() {
    JsonToXmlConverter toXmlConverter = new JsonToXmlConverter();
    XmlToJsonConverter toJsonConverter = new XmlToJsonConverter();
    JsonElement jsonElement = new JsonParser().parse(exampleJson);
    assertEquals(jsonElement, toJsonConverter.apply(toXmlConverter.apply(jsonElement)));
  }

}