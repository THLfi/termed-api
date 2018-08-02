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
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save one node
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + nodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // get one node
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldSaveAndGetSimpleVocabulary() {
    String graphId = UUID.randomUUID().toString();

    // save graph and types
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-graph.json"))
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-types.json"))
        .post("/api/graphs/" + graphId + "/types?batch=true");

    // save nodes
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-nodes.json"))
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // check that we get the same vocabulary information back
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-nodes.json"))
            .allowingExtraUnexpectedFields()
            .allowingAnyArrayOrdering());

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldPostNodeChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();

    // save graph and type
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save first node (but not a second one)
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // post changeset deleting first node and inserting second node
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(object("id", primitive(secondNodeId))));
    given(adminAuthorizedJsonSaveRequest)
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(secondNodeId));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldRecoverFromPostingAnIllegalChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();
    String thirdNodeId = UUID.randomUUID().toString();

    // save graph and type
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save only the first node
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(firstNodeId));
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + thirdNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // try posting a changeset with valid delete and broken insert
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(
            object("id", primitive(thirdNodeId)),
            object("id", primitive(secondNodeId),
                "properties", object("badAttrId", array(object("value", primitive("foo")))))));
    given(adminAuthorizedJsonSaveRequest)
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);

    // verify that nothing is changed
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(firstNodeId));
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + thirdNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldDeleteNodeBatch() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String nodeId0 = UUID.randomUUID().toString();
    String nodeId1 = UUID.randomUUID().toString();

    // save graph, type and nodes
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonSaveRequest)
        .body("[{'id':'" + nodeId0 + "'},{'id':'" + nodeId1 + "'}]")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes?batch=true");

    // check preconditions
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId0)
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId1)
        .then()
        .statusCode(HttpStatus.SC_OK);

    // delete nodes in batch
    given(adminAuthorizedJsonSaveRequest)
        .body("[{'id':'" + nodeId0 + "'},{'id':'" + nodeId1 + "'}]")
        .delete("/api/graphs/" + graphId + "/types/Concept/nodes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId0)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId1)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

}
