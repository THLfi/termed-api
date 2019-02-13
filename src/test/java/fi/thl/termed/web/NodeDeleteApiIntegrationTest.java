package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeDeleteApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  void shouldDeleteNodeBatch() {
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

  @Test
  void shouldDeleteAndDisconnectNodeBatch() {
    UUID graphId = UUID.randomUUID();

    Type termType = Type.builder()
        .id("Term", graphId)
        .build();
    Type conceptType = Type.builder()
        .id("Concept", graphId)
        .referenceAttributes(
            ReferenceAttribute.builder()
                .id("related", TypeId.of("Concept", graphId))
                .range(TypeId.of("Concept", graphId))
                .build(),
            ReferenceAttribute.builder()
                .id("term", TypeId.of("Concept", graphId))
                .range(TypeId.of("Term", graphId))
                .build())
        .build();

    Node term0 = Node.builder()
        .random(termType.identifier())
        .build();
    Node term1 = Node.builder()
        .random(termType.identifier())
        .build();
    Node concept0 = Node.builder()
        .random(conceptType.identifier())
        .addReference("term", term0.identifier())
        .addReference("term", term1.identifier())
        .build();
    Node concept1 = Node.builder()
        .random(conceptType.identifier())
        .addReference("related", concept0.identifier())
        .build();

    // save graph, types and nodes
    given(adminAuthorizedJsonSaveRequest)
        .body("{}")
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body(ImmutableList.of(termType, conceptType))
        .post("/api/graphs/" + graphId + "/types?batch=true");
    given(adminAuthorizedJsonSaveRequest)
        .body(ImmutableList.of(term0, term1, concept0, concept1))
        .post("/api/graphs/" + graphId + "/nodes?batch=true")
        .then()
        .log().body()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // delete nodes in batch with disconnect
    given(adminAuthorizedJsonSaveRequest)
        .body(ImmutableList.of(term0.identifier(), term1.identifier(), concept0.identifier()))
        .delete("/api/graphs/" + graphId + "/nodes?batch=true&disconnect=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Term/nodes/" + term0.getId())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Term/nodes/" + term1.getId())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + concept0.getId())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + concept1.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

}
