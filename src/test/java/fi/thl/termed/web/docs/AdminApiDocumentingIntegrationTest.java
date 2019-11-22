package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class AdminApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @Test
  void documentDeleteIndex() {
    given(adminAuthorizedRequest).filter(
        document("delete-index",
            operationIntro("Delete request to index triggers full re-indexing."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials"))))
        .when()
        .delete("/api/index")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedRequest).filter(
        document("delete-graph-index",
            operationIntro(
                "Delete request to graph index triggers re-indexing of given graph."),
            pathParameters(
                parameterWithName("id")
                    .description("Graph identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials"))))
        .when()
        .delete("/api/graphs/{id}/index", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedRequest).filter(
        document("delete-type-index",
            operationIntro(
                "Delete request to graph type index triggers re-indexing of given type."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials"))))
        .when()
        .delete("/api/graphs/{graphId}/types/{id}/index",
            personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  void documentDeleteCaches() {
    given(adminAuthorizedRequest).filter(
        document("delete-caches",
            operationIntro("Delete request to caches triggers cache invalidation")))
        .when()
        .delete("/api/caches")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  void documentDeleteRevisions() {
    given(adminAuthorizedRequest).filter(
        document("delete-revisions",
            operationIntro("Delete request to revisions purges all revision history entries")))
        .when()
        .delete("/api/revisions")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  void revertGraphNodesToRevision() {
    given(adminAuthorizedJsonSaveRequest).filter(
        document("revert-graph-nodes-to-revisions",
            operationIntro(
                "Request to to copy all graph nodes from given revision to current revision. "
                    + "Reverting might fail if types have been changed since given revision. "
                    + "This can be worked around by first manually reverting types. "
                    + "Reverting might also not work if graph refers to another graphs. "
                    + "Finally, if full revert to past state is needed, `DELETE /api/graphs/{id}/nodes` "
                    + "might need to be called first."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier")),
            requestParameters(
                parameterWithName("targetRevision")
                    .description(
                        "Required number telling the revision from which nodes are copied."))))
        .when()
        .post("/api/graphs/{graphId}/nodes?targetRevision=25", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

}
