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

    // save graph and type
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save one node
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + nodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // get one node
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // clean up
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldSaveAndGetSimpleVocabulary() {
    String graphId = UUID.randomUUID().toString();

    // save graph and types
    given(adminAuthorizedJsonRequest)
        .body(resourceToString("examples/termed/animals-graph.json"))
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonRequest)
        .body(resourceToString("examples/termed/animals-types.json"))
        .post("/api/graphs/" + graphId + "/types?batch=true");

    // save nodes
    given(adminAuthorizedJsonRequest)
        .body(resourceToString("examples/termed/animals-nodes.json"))
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // check that we get the same vocabulary information back
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-nodes.json"))
            .allowingExtraUnexpectedFields()
            .allowingAnyArrayOrdering());

    // clean up
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldPostNodeChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();

    // save graph and type
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save first node (but not a second one)
    given(adminAuthorizedJsonRequest)
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // post changeset deleting first node and inserting second node
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(object("id", primitive(secondNodeId))));
    given(adminAuthorizedJsonRequest)
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(secondNodeId));

    // clean up
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId);
  }

}
