package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeGraphQLApiDocumentingIntegrationTest extends BaseNodeApiDocumentingIntegrationTest {

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
                            + "`data.nodes` structure. Default value is `true` meaning that "
                            + "response body is an array of Termed Nodes.")),
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
