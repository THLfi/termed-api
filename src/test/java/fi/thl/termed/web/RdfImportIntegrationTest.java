package fi.thl.termed.web;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import fi.thl.termed.util.io.ResourceUtils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class RdfImportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveRdfVocabulary() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-graph.json"))
        .when()
        .put("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save graph types
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-types.json"))
        .when()
        .post("/api/graphs/" + graphId + "/types?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // save graph nodes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/rdf+xml")
        .body(ResourceUtils.resourceToString("examples/nasa/example-nodes.rdf"))
        .when()
        .post("/api/graphs/" + graphId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
