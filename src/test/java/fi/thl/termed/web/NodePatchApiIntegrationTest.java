package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.gson.JsonElement;
import fi.thl.termed.util.UUIDs;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodePatchApiIntegrationTest extends BaseApiIntegrationTest {

  private String graphId = UUID.randomUUID().toString();

  @BeforeEach
  void insertTestData() {
    // save graph, types and nodes
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-graph.json"))
        .put("/api/graphs/{graphId}?mode=insert", graphId);
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-types.json"))
        .post("/api/graphs/{graphId}/types?batch=true&mode=insert", graphId);
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-nodes.json"))
        .post("/api/graphs/{graphId}/types/Concept/nodes?batch=true&mode=insert", graphId);

    // verify that we get the same vocabulary information back via basic node api
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes", graphId)
        .then()
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
  void shouldPatchNodeById() {
    UUID animalNodeId = UUIDs.fromString("194a6da0-9e1a-4f4c-be21-0fcfd44e81e8");

    // should patch prefLabel by appending "Djuren"
    given(adminAuthorizedJsonSaveRequest)
        .body(object("properties", object("prefLabel", array(
            object("value", primitive("Djuren")),
            object("lang", primitive("sv"))))))
        .patch("/api/graphs/{graphId}/types/Concept/nodes/{id}?append=true", graphId, animalNodeId)
        .then().statusCode(HttpStatus.SC_OK);

    // verify changes
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes/{id}", graphId, animalNodeId)
        .then().statusCode(HttpStatus.SC_OK)
        .body("properties.prefLabel[2].value", equalTo("Djuren"));
  }

  @Test
  void shouldPatchAllNodesOfType() {
    // should replace each prefLabel by "Patched!"
    given(adminAuthorizedJsonSaveRequest)
        .body(object("properties", object("prefLabel", array(
            object("value", primitive("Patched!"))))))
        .patch("/api/graphs/{graphId}/types/Concept/nodes?append=false&where=", graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes?max=-1", graphId)
        .then().statusCode(HttpStatus.SC_OK)
        .body("properties.prefLabel[0].value", contains("Patched!"));
  }


  @Test
  void shouldPatchBatchOfNodesOfType() {
    UUID vertebratesId = UUIDs.fromString("26e6d6e4-6189-4c83-b2d9-637044fbdb65");
    UUID invertebratesId = UUIDs.fromString("7b0ae701-d8c9-463d-9707-48e92494bb59");

    JsonElement prefLabel = object("prefLabel", array(object("value", primitive("Patched!"))));

    JsonElement vertebrates = object(
        "id", primitive(vertebratesId.toString()),
        "properties", prefLabel);
    JsonElement invertebrates = object(
        "id", primitive(invertebratesId.toString()),
        "properties", prefLabel);

    // should replace prefLabel by "Patched!" for each in given batch
    given(adminAuthorizedJsonSaveRequest)
        .body(array(vertebrates, invertebrates))
        .patch("/api/graphs/{graphId}/types/Concept/nodes?append=false&batch=true", graphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes/{id}", graphId, vertebratesId)
        .then().statusCode(HttpStatus.SC_OK)
        .body("properties.prefLabel[0].value", equalTo("Patched!"))
        // check that references are not cleared (as they would be with put or post)
        .body("references.broader[0].id", equalTo("194a6da0-9e1a-4f4c-be21-0fcfd44e81e8"));
    given(adminAuthorizedJsonSaveRequest)
        .get("/api/graphs/{graphId}/types/Concept/nodes/{id}", graphId, invertebratesId)
        .then().statusCode(HttpStatus.SC_OK)
        .body("properties.prefLabel[0].value", equalTo("Patched!"));
  }

}
