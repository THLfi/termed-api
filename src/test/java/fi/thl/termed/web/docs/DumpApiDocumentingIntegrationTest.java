package fi.thl.termed.web.docs;

import static fi.thl.termed.web.docs.DocsExampleData.exampleGraph;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0Id;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.groupType;
import static fi.thl.termed.web.docs.DocsExampleData.personType;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import fi.thl.termed.domain.Dump;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DumpApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

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
  void documentDumpGraph() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-graph-dump",
            operationIntro("Get a dump by graph id."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                subsectionWithPath("graphs")
                    .description("List of graphs in the dump. "
                        + "In this case, contains only one graph."),
                subsectionWithPath("types")
                    .description("List of types in the dump."),
                subsectionWithPath("nodes")
                    .description("List of nodes in the dump."))))
        .when()
        .get("/api/graphs/{graphId}/dump",
            exampleNode0Id.getTypeGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentDump() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-dump",
            operationIntro("Get a dump containing all or select graphs."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("graphId").optional()
                    .description("Optional parameter for limiting graph included in the dump. "
                        + "If parameter is repeated, all given graphs are included in the dump.")),
            responseFields(
                subsectionWithPath("graphs")
                    .description("List of graphs in the dump."),
                subsectionWithPath("types")
                    .description("List of types in the dump."),
                subsectionWithPath("nodes")
                    .description("List of nodes in the dump."))))
        .when()
        .get("/api/dump")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentRestoreDump() {
    Dump dump = new Dump(
        Stream.of(exampleGraph),
        Stream.of(personType, groupType),
        Stream.of(exampleNode0, exampleNode1));

    given(adminAuthorizedJsonSaveRequest)
        .filter(document("post-a-dump",
            operationIntro("Restore a dump. Tries to save all graphs, types and nodes in the dump. "
                + "Graphs, types and nodes are each saved atomically but whole dump is not. I.e. "
                + "error on saving nodes leaves updated graphs and types."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("generateUris").optional()
                    .description("Optional parameter to define whether missing URIs are "
                        + "automatically generated. Default value is `false`."),
                parameterWithName("generateCodes").optional()
                    .description("Optional parameter to define whether missing codes are "
                        + "automatically generated. Default value is `false`."))))
        .when()
        .body(dump)
        .post("/api/dump")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentCopyDump() {
    UUID targetGraphId = UUID.randomUUID();

    given(adminAuthorizedJsonSaveRequest)
        .filter(document("copy-a-dump",
            operationIntro("Copy graph, types and nodes into a new graph."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            pathParameters(
                parameterWithName("sourceGraphId")
                    .description("Source graph identifier (UUID)")),
            requestParameters(
                parameterWithName("copy")
                    .description("Required parameter, must be `true` to trigger copying."),
                parameterWithName("typesOnly").optional()
                    .description("Optional parameter, limit copying to graph and types only. "
                        + "Default value is `false`."),
                parameterWithName("targetGraphId").optional()
                    .description("Optional parameter to define target graph to where dump is "
                        + "copied to. Default value is new random UUID."),
                parameterWithName("generateUris").optional()
                    .description("Optional parameter to define whether missing URIs are "
                        + "automatically generated. Default value is `false`."),
                parameterWithName("generateCodes").optional()
                    .description("Optional parameter to define whether missing codes are "
                        + "automatically generated. Default value is `false`."))))
        .when()
        .post("/api/graphs/{sourceGraphId}/dump?copy=true&targetGraphId={targetGraphId}",
            exampleNode0Id.getTypeGraphId(),
            targetGraphId)
        .then()
        .statusCode(HttpStatus.SC_OK);

    // remove the copy
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}/nodes", targetGraphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}/types", targetGraphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", targetGraphId)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
