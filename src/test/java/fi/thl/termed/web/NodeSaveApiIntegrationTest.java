package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.web.ApiExampleData.exampleGraphId;
import static fi.thl.termed.web.ApiExampleData.exampleNode1;
import static fi.thl.termed.web.ApiExampleData.exampleNode1Id;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import fi.thl.termed.domain.StrictLangValue;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeSaveApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  void shouldSaveAndGetTrivialResource() {
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
  void shouldSaveAndGetSimpleVocabulary() {
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
  void shouldPatchNodeById() {
    // save graph and types
    given(adminAuthorizedJsonSaveRequest)
        .body(ApiExampleData.exampleGraph)
        .put("/api/graphs/{graphId}?mode=insert", exampleGraphId.getId());
    given(adminAuthorizedJsonSaveRequest)
        .body(ApiExampleData.personType)
        .post("/api/graphs/{graphId}/types", exampleGraphId.getId());

    // save a node with
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleNode1)
        .post("/api/graphs/{graphId}/types/Person/nodes", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Person/nodes/{id}",
            exampleGraphId.getId(), exampleNode1Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("properties.email[0].value",
            equalTo(exampleNode1.getFirstPropertyValue("email")
                .map(StrictLangValue::getValue)
                .orElseThrow(AssertionError::new)))
        .body("properties.name[0].value",
            equalTo(exampleNode1.getFirstPropertyValue("name")
                .map(StrictLangValue::getValue)
                .orElseThrow(AssertionError::new)));

    // patch name (replace person name with with a new one)
    given(adminAuthorizedJsonSaveRequest)
        .body("{\"properties\": { \"name\": [ { \"value\": \"PatchedName\"} ] } }")
        .patch("/api/graphs/{graphId}/types/Person/nodes/{id}?append=false",
            exampleGraphId.getId(), exampleNode1Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);

    // verify changes
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Person/nodes/{id}",
            exampleGraphId.getId(), exampleNode1Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("properties.email[0].value",
            equalTo(exampleNode1.getFirstPropertyValue("email")
                .map(StrictLangValue::getValue)
                .orElseThrow(AssertionError::new)))
        .body("properties.name[0].value", equalTo("PatchedName"));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/nodes", exampleGraphId.getId());
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/types", exampleGraphId.getId());
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}", exampleGraphId.getId());
  }

}
