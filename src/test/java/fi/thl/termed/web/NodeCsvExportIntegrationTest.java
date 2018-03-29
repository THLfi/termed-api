package fi.thl.termed.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

import fi.thl.termed.util.spring.http.MediaTypes;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class NodeCsvExportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveAndGetTrivialResource() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";
    String nodeId = UUID.randomUUID().toString();

    // save test data
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?insert=true");
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + nodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes");

    // get node data in csv
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes.csv")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .body(not(isEmptyString()));

    // clean up
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId);
  }

}
