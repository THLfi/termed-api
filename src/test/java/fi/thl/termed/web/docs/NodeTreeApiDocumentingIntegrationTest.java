package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraph;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0Id;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.groupType;
import static fi.thl.termed.web.docs.DocsExampleData.personType;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

class NodeTreeApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

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
  void documentGetNodeTreeById() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-node-tree",
            operationIntro("Get a node tree by id in given graph."),
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
                parameterWithName("select").optional()
                    .description(
                        "Optional list of fields that are selected from tree. "
                            + "List values can be comma separated or request parameter can be repeated."
                            + "E.g. select=id,type,number,properties.name,references.knows"),
                parameterWithName("pretty").optional()
                    .description("Optional boolean parameter to specify if result JSON should "
                        + "be pretty printed. Default value is `false`. ")),
            responseFields(
                fieldWithPath("id")
                    .description("Optional Node identifier (UUID)"),
                subsectionWithPath("type")
                    .optional().type(JsonFieldType.OBJECT)
                    .description("Optional Type identifier object"),
                fieldWithPath("code")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional identifying code for the node"),
                fieldWithPath("uri")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional identifying URI for the node"),
                fieldWithPath("number")
                    .description("Optional Node number"),
                fieldWithPath("createdBy")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional Creator of the node (username)"),
                fieldWithPath("createdDate")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional Node created date in ISO format"),
                fieldWithPath("lastModifiedBy")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional Last modifier of the node (username)"),
                fieldWithPath("lastModifiedDate")
                    .optional().type(JsonFieldType.STRING)
                    .description("Optional Node last modified date in ISO format"),
                subsectionWithPath("properties")
                    .description("Optional map of node properties where keys are attribute IDs "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."),
                subsectionWithPath("references")
                    .optional().type(JsonFieldType.OBJECT)
                    .description("Optional map of node references where keys are attribute IDs "
                        + "(e.g. `knows`) and values are a lists of node trees."),
                subsectionWithPath("referrers")
                    .optional().type(JsonFieldType.OBJECT)
                    .description("Optional map of node referrers with same structure as "
                        + "references."))))
        .when()
        .get(
            "/api/graphs/{graphId}/types/{typeId}/node-trees/{id}?select=id,type,number,properties.*,references.knows&pretty=true",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetTypeNodeTrees() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-type-node-trees",
            operationIntro(
                "Returns an array of node trees in given graph of given type."),
            pathParameters(
                parameterWithName("graphId").description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestParameters(
                parameterWithName("select").optional()
                    .description(
                        "Optional list of fields that are selected from tree."),
                parameterWithName("where").optional()
                    .description(
                        "Optional query to specify nodes by properties, references etc. "
                            + "For example where=properties.name:John"),
                parameterWithName("sort").optional()
                    .description(
                        "Optional list of fields to sort results, e.g. sort=properties.prefLabel.fi"),
                parameterWithName("max").optional()
                    .description(
                        "Optional parameter to specify max results to return. "
                            + "Value -1 means that all values are returned."),
                parameterWithName("pretty").optional()
                    .description("Optional boolean parameter to specify if result JSON should "
                        + "be pretty printed. Default value is `false`. "))))
        .when()
        .get(
            "/api/graphs/{graphId}/types/{typeId}/node-trees?select=id,properties.name&where=properties.name:John&pretty=true",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetGraphNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-graph-node-trees",
            operationIntro(
                "Returns an array of node trees in given graph."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            requestParameters(
                parameterWithName("select").optional()
                    .description(
                        "Optional list of fields that are selected from tree."),
                parameterWithName("where").optional()
                    .description(
                        "Optional query to specify nodes by properties, references etc. "
                            + "For example where=properties.name:John"),
                parameterWithName("sort").optional()
                    .description(
                        "Optional list of fields to sort results, e.g. sort=properties.prefLabel.fi"),
                parameterWithName("max").optional()
                    .description(
                        "Optional parameter to specify max results to return. "
                            + "Value -1 means that all values are returned."),
                parameterWithName("pretty").optional()
                    .description("Optional boolean parameter to specify if result JSON should "
                        + "be pretty printed. Default value is `false`. "))))
        .when()
        .get("/api/graphs/{graphId}/node-trees", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetAllNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-all-node-trees",
            operationIntro(
                "Returns an array of node trees in any graph."),
            requestParameters(
                parameterWithName("select").optional()
                    .description(
                        "Optional list of fields that are selected from tree."),
                parameterWithName("where").optional()
                    .description(
                        "Optional query to specify nodes by properties, references etc. "
                            + "For example where=properties.name:John"),
                parameterWithName("sort").optional()
                    .description(
                        "Optional list of fields to sort results, e.g. sort=properties.prefLabel.fi"),
                parameterWithName("max").optional()
                    .description(
                        "Optional parameter to specify max results to return. "
                            + "Value -1 means that all values are returned."),
                parameterWithName("pretty").optional()
                    .description("Optional boolean parameter to specify if result JSON should "
                        + "be pretty printed. Default value is `false`. "))))
        .when()
        .get("/api/node-trees")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

}
