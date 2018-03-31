package fi.thl.termed.web;

import static fi.thl.termed.util.json.JsonUtils.getJsonResource;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class GraphApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveTrivialGraph() {
    String graphId = UUID.randomUUID().toString();

    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    given(adminAuthorizedRequest)
        .delete("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldSaveAndGetGraph() throws IOException {
    String graphId = UUID.randomUUID().toString();

    JsonObject skosGraph = getJsonResource("examples/skos/example-skos-graph.json")
        .getAsJsonObject();
    skosGraph.addProperty("id", graphId);

    given(adminAuthorizedJsonSaveRequest)
        .body(skosGraph.toString())
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // get the persisted graph and compare to original allowing extra fields such as timestamps
    given(adminAuthorizedJsonGetRequest)
        .when()
        .get("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(skosGraph.toString()).allowingExtraUnexpectedFields());

    given(adminAuthorizedRequest)
        .delete("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
