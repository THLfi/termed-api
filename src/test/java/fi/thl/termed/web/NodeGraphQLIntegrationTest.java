package fi.thl.termed.web;

import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static fi.thl.termed.web.ExampleData.exampleGraph;
import static fi.thl.termed.web.ExampleData.exampleGraphId;
import static fi.thl.termed.web.ExampleData.exampleNode0;
import static fi.thl.termed.web.ExampleData.exampleNode0Id;
import static fi.thl.termed.web.ExampleData.exampleNode1;
import static fi.thl.termed.web.ExampleData.personType;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

public class NodeGraphQLIntegrationTest extends BaseApiIntegrationTest {

  @Before
  public void insertExampleData() {
    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(exampleGraph))
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(personType))
        .post("/api/graphs/" + exampleGraphId.getId() + "/types?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(asList(exampleNode0, exampleNode1)))
        .post("/api/graphs/" + exampleGraphId.getId() + "/types/" + personType.getId()
            + "/nodes?batch=true&mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @After
  public void deleteExampleData() {
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId() + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId() + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + exampleGraphId.getId());
  }

  @Test
  public void shouldSaveAndGetNodeWithGraphQL() {
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
