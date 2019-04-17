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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import fi.thl.termed.domain.StrictLangValue;
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
  void shouldGetAllNodesOfGivenType() {
    String exampleGraphQLQuery = "{"
        + " nodes {"
        + "   id, properties { name { value } }, references { knows { id, uri } }"
        + " }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body("nodes.id", hasItems(
            exampleNode0.getId().toString(),
            exampleNode1.getId().toString()));
  }

  @Test
  void shouldGetNodeByIdOfGivenType() {
    String exampleGraphQLQuery = "{"
        + " nodes(where: \"id:" + exampleNode0Id.getId() + "\") {"
        + "   id, properties { name { value } }, references { knows { id, uri } }"
        + " }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body(sameJSONAs(
            object("nodes",
                array(object(
                    "id", primitive(exampleNode0.getId().toString()),
                    "properties", object("name", array(object("value", primitive("John")))),
                    "references", object("knows",
                        array(object(
                            "id", primitive(exampleNode1.getId().toString()),
                            "uri", primitive(exampleNode1.getUri().orElse(""))))))))
                .toString()));
  }

  @Test
  void shouldGetNodeByPropertyValueOfGivenType() {
    String exampleNode0Name = exampleNode0
        .getFirstPropertyValue("name")
        .map(StrictLangValue::getValue)
        .orElse(null);

    String exampleGraphQLQuery = "{"
        + " nodes(where: \"p.name:\\\"" + exampleNode0Name + "\\\"\") { id }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body("nodes.id", contains(exampleNode0Id.getId().toString()));
  }

  @Test
  void shouldGetMaxNodesOfGivenType() {
    String exampleGraphQLQuery = "{"
        + " nodes(max: 1) {"
        + "   id, properties { name { value } }, references { knows { id, uri } }"
        + " }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body("nodes", hasSize(1));
  }

  @Test
  void shouldGetSortedNodesOfGivenType() {
    String exampleGraphQLQuery = "{"
        + " nodes(sort: [\"properties.name.sortable\"]) {"
        + "   id, properties { name { value } }"
        + " }"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body("nodes.properties.name.value.flatten()", contains(
            exampleNode1.getFirstPropertyValue("name")
                .map(StrictLangValue::getValue).orElse(null),
            exampleNode0.getFirstPropertyValue("name")
                .map(StrictLangValue::getValue).orElse(null)));
  }

  @Test
  void shouldReturnBadRequestOnIllegalQuery() {
    String exampleGraphQLQuery = "{"
        + " foobar"
        + "}";

    given(adminAuthorizedRequest)
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            exampleGraphId.getId(), personType.getId())
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

}
