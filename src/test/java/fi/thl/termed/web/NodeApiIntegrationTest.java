package fi.thl.termed.web;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class NodeApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveAndGetTrivialResource() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";
    String nodeId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + graphId + "'}")
        .when()
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save type
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + typeId + "'}")
        .when()
        .post("/api/graphs/" + graphId + "/types")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(typeId));

    // save one node
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + nodeId + "'}")
        .when()
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // get one node
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));
  }

  @Test
  public void shouldSaveAndGetSimpleVocabulary() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-graph.json"))
        .when()
        .put("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save types
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-types.json"))
        .when()
        .post("/api/graphs/" + graphId + "/types?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // save nodes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-nodes.json"))
        .when()
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // check that we get the same vocabulary information back (bypass index as its built async)
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/graphs/" + graphId + "/types/Concept/nodes?bypassIndex=true")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-nodes.json"))
                  .allowingExtraUnexpectedFields()
                  .allowingAnyArrayOrdering());
  }

}
