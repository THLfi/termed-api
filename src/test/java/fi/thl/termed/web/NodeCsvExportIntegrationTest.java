package fi.thl.termed.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

import fi.thl.termed.util.spring.http.MediaTypes;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

public class NodeCsvExportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveAndGetTrivialResource() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";
    String nodeId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .body("{'id':'" + graphId + "'}")
        .when()
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save type
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .body("{'id':'" + typeId + "'}")
        .when()
        .post("/api/graphs/" + graphId + "/types")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(typeId));

    // save one node
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .body("{'id':'" + nodeId + "'}")
        .when()
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // get one node
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .when()
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes.csv")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .body(Matchers.not(Matchers.isEmptyString()));
  }

}
