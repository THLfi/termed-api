package fi.thl.termed.web;

import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static fi.thl.termed.web.ApiExampleData.exampleGraph;
import static fi.thl.termed.web.ApiExampleData.exampleGraphId;
import static fi.thl.termed.web.ApiExampleData.exampleNode0;
import static fi.thl.termed.web.ApiExampleData.exampleNode0Id;
import static fi.thl.termed.web.ApiExampleData.exampleNode1;
import static fi.thl.termed.web.ApiExampleData.personType;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class NodeGraphQLIntegrationTest extends BaseApiIntegrationTest {

  @BeforeEach
  void insertExampleData() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(personType)
        .post("/api/graphs/" + exampleGraphId.getId() + "/types?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(asList(exampleNode0, exampleNode1))
        .post("/api/graphs/" + exampleGraphId.getId() + "/types/" + personType.getId()
            + "/nodes?batch=true&mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @AfterEach
  void deleteExampleData() {
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId() + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId() + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId());
  }

  @Test
  void shouldGetNodeWithGraphQL() {
    String exampleGraphQLQuery = "{"
        + " node(id: \"" + exampleNode0Id.getId() + "\") {"
        + "   id, properties { name { value } }, references { knows { id, uri } }"
        + " }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/" + exampleGraphId.getId()
            + "/types/" + personType.getId()
            + "/nodes/graphql")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body(sameJSONAs(
            object("node",
                object(
                    "id", primitive(exampleNode0.getId().toString()),
                    "properties", object("name", array(object("value", primitive("John")))),
                    "references", object("knows",
                        array(object(
                            "id", primitive(exampleNode1.getId().toString()),
                            "uri", primitive(exampleNode1.getUri().orElse("")))))))
                .toString()));
  }

}
