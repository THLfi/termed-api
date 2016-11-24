package fi.thl.termed.domain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import org.junit.Test;

import fi.thl.termed.web.external.node.dto.NodeQueryParser;

import static org.junit.Assert.assertEquals;

public class NodeQueryParserTest {

  @Test
  public void shouldParseDtoQuery() {
    ListMultimap<String, String> queryMap = ArrayListMultimap.create();

    queryMap.put("select.properties", "prefLabel");
    queryMap.put("select.properties", "altLabel");
    queryMap.put("select.references", "broader");
    queryMap.put("where.references.related", "c7e9595e-1e04-437c-9bc9-2f9dcba88e11");
    queryMap.put("where.references.related", "c7b4dc7a-e155-4331-b856-f774559eea18");
    queryMap.put("recurse.references.broader", "1");
    queryMap.put("limit", "50");

    assertEquals("{\n"
                 + "  \"select\": {\n"
                 + "    \"type\": false,\n"
                 + "    \"graph\": false,\n"
                 + "    \"audit\": false,\n"
                 + "    \"properties\": [\n"
                 + "      \"prefLabel\",\n"
                 + "      \"altLabel\"\n"
                 + "    ],\n"
                 + "    \"references\": [\n"
                 + "      \"broader\"\n"
                 + "    ],\n"
                 + "    \"referrers\": []\n"
                 + "  },\n"
                 + "  \"where\": {\n"
                 + "    \"properties\": {},\n"
                 + "    \"references\": {\n"
                 + "      \"related\": [\n"
                 + "        \"c7e9595e-1e04-437c-9bc9-2f9dcba88e11\",\n"
                 + "        \"c7b4dc7a-e155-4331-b856-f774559eea18\"\n"
                 + "      ]\n"
                 + "    }\n"
                 + "  },\n"
                 + "  \"recurse\": {\n"
                 + "    \"references\": {\n"
                 + "      \"broader\": 1\n"
                 + "    },\n"
                 + "    \"referrers\": {}\n"
                 + "  },\n"
                 + "  \"sort\": [],\n"
                 + "  \"max\": 50\n"
                 + "}", NodeQueryParser.parse(Multimaps.asMap(queryMap)).toString());
  }

}