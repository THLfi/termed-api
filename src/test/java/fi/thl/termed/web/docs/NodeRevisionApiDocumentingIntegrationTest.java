package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0Id;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import fi.thl.termed.domain.Revision;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeRevisionApiDocumentingIntegrationTest extends BaseNodeApiDocumentingIntegrationTest {

  @Test
  void documentGetNodeRevisions() {
    // do an extra update to populate revision history
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleNode0)
        .post("/api/graphs/{graphId}/nodes", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-node-revisions",
            operationIntro("Get an array of node revisions by Node ID. Revisions are returned in "
                + "descending order. Objects in the array contain only the \"metadata\". Complete "
                + "revisions need to be loaded separately."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("max")
                    .description("Optional parameter for limiting the number returned revisions. "
                        + "Default value is `-1` returning all node revisions.")),
            responseFields(
                fieldWithPath("[].number")
                    .description("Revision number, integer."),
                fieldWithPath("[].author")
                    .description("Author of the revision (username)."),
                fieldWithPath("[].date")
                    .description("Date of the revision in ISO format."),
                subsectionWithPath("[].object")
                    .description("Object revision, in this case just the Node identifier."))))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions?max=3",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetNodeRevision() {
    Revision[] revisionArray = given(superuserAuthorizedRequest)
        .when()
        .get("/api/revisions")
        .then()
        .extract()
        .as(Revision[].class);

    long greatestRevisionNumber = Stream.of(revisionArray)
        .map(Revision::getNumber)
        .mapToLong(Long::longValue)
        .max()
        .orElse(1L);

    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-node-revision",
            operationIntro("Get a node revisions by node id and revision number."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)"),
                parameterWithName("number")
                    .description("Revision number (integer). If there is no revision with exactly "
                        + "same revision number, finds the latest revision in respect to `number`. "
                        + "I.e. a version that was valid at given revision. This functionality is "
                        + "useful for traversing graph in past state.")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("number")
                    .description("Revision number, integer."),
                fieldWithPath("author")
                    .description("Author of the revision (username)."),
                fieldWithPath("date")
                    .description("Date of the revision in ISO format."),
                fieldWithPath("type")
                    .description(
                        "Revision type, possible values are `INSERT`, `UPDATE`, `DELETE`."),
                subsectionWithPath("object")
                    .description("Node as it was in given revision."))))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions/{number}",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId(),
            greatestRevisionNumber)
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

}
