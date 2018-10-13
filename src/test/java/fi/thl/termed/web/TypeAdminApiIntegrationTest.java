package fi.thl.termed.web;

import static fi.thl.termed.web.ApiExampleData.exampleGraph;
import static fi.thl.termed.web.ApiExampleData.exampleGraphId;
import static fi.thl.termed.web.ApiExampleData.exampleNode0;
import static fi.thl.termed.web.ApiExampleData.exampleNode1;
import static fi.thl.termed.web.ApiExampleData.personType;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;

import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import java.util.Objects;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class TypeAdminApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveAndGetTrivialResource() {
    // set up
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonSaveRequest)
        .body(personType)
        .post("/api/graphs/{graphId}/types?mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonSaveRequest)
        .body(asList(exampleNode0, exampleNode1))
        .post("/api/graphs/{graphId}/nodes?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // rename text and ref attributes
    given(adminAuthorizedJsonSaveRequest)
        .body(personType)
        .post("/api/graphs/{graphId}/types/{typeId}/textAttributes/name?newId=firstName",
            exampleGraph.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    given(adminAuthorizedJsonSaveRequest)
        .body(personType)
        .post("/api/graphs/{graphId}/types/{typeId}/referenceAttributes/knows?newId=friends",
            exampleGraph.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleGraphId.getId(), personType.getId(), exampleNode0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("properties.firstName[0].value", equalTo(exampleNode0
            .getFirstPropertyValue("name")
            .map(StrictLangValue::getValue)
            .orElse(null)))
        .body("references.friends[0].id", equalTo(exampleNode0
            .getFirstReferenceValue("knows")
            .map(NodeId::getId)
            .map(Objects::toString)
            .orElse(null)));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraph.getId() + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraph.getId() + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraph.getId());
  }

}