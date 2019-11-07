package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraph;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.groupType;
import static fi.thl.termed.web.docs.DocsExampleData.personType;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodeGraphQLApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @BeforeEach
  void insertExampleGraphAndTypesAndNodes() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(asList(personType, groupType))
        .post("/api/graphs/{graphId}/types?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedJsonSaveRequest)
        .body(asList(exampleNode0, exampleNode1))
        .post("/api/graphs/{graphId}/nodes?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @AfterEach
  void deleteExampleGraphAndTypesAndNodes() {
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}/nodes", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}/types", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentGetNodesByGraphQL() {
    String exampleGraphQLQuery = "{\n"
        + "  nodes(where: \"props.name:Jo*\") {\n"
        + "    id\n"
        + "    properties {\n"
        + "      name { value }\n"
        + "    }\n"
        + "    references {\n"
        + "      knows { id, uri } \n"
        + "    }\n"
        + "  }\n"
        + "}";

    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-nodes-by-graphql",
            operationIntro(
                "Returns an array of nodes of given type loaded based on given GraphQL query. "
                    + "Request body contains the GraphQL query. Response body contains an array "
                    + "of Termed Nodes."),
            requestParameters(
                parameterWithName("unwrapResults").optional()
                    .description(
                        "Optional boolean parameter to specify whether or not to remove outer "
                            + "`data.nodes` structure (data.<root-query-object> structure is "
                            + "defined by GraphQL). Default value is `true` meaning that response "
                            + "body is an array of Termed Nodes.")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"))))
        .when()
        .body(exampleGraphQLQuery)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes/graphql",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

}
