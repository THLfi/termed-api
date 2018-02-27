package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.gson.JsonObject;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

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
  public void shouldSaveAndGetSimpleVocabulary() {
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

    // check that we get the same vocabulary information back
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/graphs/" + graphId + "/types/Concept/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-nodes.json"))
            .allowingExtraUnexpectedFields()
            .allowingAnyArrayOrdering());
  }

  @Test
  public void shouldPostNodeChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // save type
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // save first node
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // post changeset deleting first node and inserting second node
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(object("id", primitive(secondNodeId))));
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(secondNodeId));
  }

}
