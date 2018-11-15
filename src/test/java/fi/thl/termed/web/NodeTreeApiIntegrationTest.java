package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.util.Map;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodeTreeApiIntegrationTest extends BaseApiIntegrationTest {

  private String graphId = UUID.randomUUID().toString();

  @BeforeEach
  void insertTestData() {
    // save graph, types and nodes
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-graph.json"))
        .put("/api/graphs/{graphId}?mode=insert", graphId)
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-types.json"))
        .post("/api/graphs/{graphId}/types?batch=true&mode=insert", graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-nodes.json"))
        .post("/api/graphs/{graphId}/types/Concept/nodes?batch=true&mode=insert", graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify that we get the same vocabulary information back via basic node api
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes", graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-nodes.json"))
            .allowingExtraUnexpectedFields()
            .allowingAnyArrayOrdering());
  }

  @AfterEach
  void removeTestData() {
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/nodes", graphId);
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/types", graphId);
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}", graphId);
  }

  @Test
  void shouldSelectId() {
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/Concept/node-trees?select=id&max=-1", graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("$", everyItem(hasKey("id")))
        .body("$", everyItem(
            PredicateBasedMatcher.<Map>of("an object with exactly one key", m -> m.size() == 1)));
  }

  @Test
  void shouldReturnTree() {
    given(adminAuthorizedJsonGetRequest)
        .param("select", "code")
        .param("select", "referrers.broader:10")
        .param("where", "code:animals")
        .get("/api/graphs/{graphId}/types/Concept/node-trees", graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .log().body()
        .body(sameJSONAs("["
            + "  {"
            + "    'code': 'animals',"
            + "    'referrers': {"
            + "      'broader': ["
            + "        {"
            + "          'code': 'invertebrates'"
            + "        },"
            + "        {"
            + "          'code': 'vertebrates',"
            + "          'referrers': {"
            + "            'broader': ["
            + "              {"
            + "                'code': 'reptiles'"
            + "              },"
            + "              {"
            + "                'code': 'mammals',"
            + "                'referrers': {"
            + "                  'broader': ["
            + "                    {"
            + "                      'code': 'rodents',"
            + "                      'referrers': {"
            + "                        'broader': ["
            + "                          {"
            + "                            'code': 'voles'"
            + "                          },"
            + "                          {"
            + "                            'code': 'beavers'"
            + "                          }"
            + "                        ]"
            + "                      }"
            + "                    }"
            + "                  ]"
            + "                }"
            + "              },"
            + "              {"
            + "                'code': 'birds'"
            + "              }"
            + "            ]"
            + "          }"
            + "        }"
            + "      ]"
            + "    }"
            + "  }"
            + "]")
            // ordering is not guaranteed for referrers
            .allowingAnyArrayOrdering());
  }

}
