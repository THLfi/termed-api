package fi.thl.termed.web;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.ExampleData.exampleGraph;
import static fi.thl.termed.web.ExampleData.exampleGraphId;
import static fi.thl.termed.web.ExampleData.exampleNode0;
import static fi.thl.termed.web.ExampleData.exampleNode0Id;
import static fi.thl.termed.web.ExampleData.exampleNode1;
import static fi.thl.termed.web.ExampleData.groupType;
import static fi.thl.termed.web.ExampleData.personType;
import static fi.thl.termed.web.ExampleData.personTypeId;
import static fi.thl.termed.web.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodeApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @Before
  public void insertExampleGraphAndTypes() {
    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(exampleGraph))
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(asList(personType, groupType)))
        .post("/api/graphs/{graphId}/types?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(asList(exampleNode0, exampleNode1)))
        .post("/api/graphs/{graphId}/nodes?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @After
  public void deleteExampleGraphAndTypesAndNodes() {
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
  public void documentGetNodeById() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-node",
            operationIntro("Get a node by id in given graph."),
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
            responseFields(
                fieldWithPath("id")
                    .description("Node identifier (UUID)"),
                subsectionWithPath("type")
                    .description("Type identifier object"),
                fieldWithPath("code")
                    .description("Optional identifying code for the node"),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the node"),
                fieldWithPath("number")
                    .description("Node number"),
                fieldWithPath("createdBy")
                    .description("Creator of the node (username)"),
                fieldWithPath("createdDate")
                    .description("Node created date in ISO format"),
                fieldWithPath("lastModifiedBy")
                    .description("Last modifier of the node (username)"),
                fieldWithPath("lastModifiedDate")
                    .description("Node last modified date in ISO format"),
                subsectionWithPath("properties")
                    .description("Optional map of node properties where keys are attribute IDs "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."),
                subsectionWithPath("references")
                    .description("Optional map of node references where keys are attribute IDs "
                        + "(e.g. `knows`) and values are a lists of node ids."),
                subsectionWithPath("referrers")
                    .description("Optional map of node referrers with same structure as "
                        + "references."))))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetTypeNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-type-nodes",
            operationIntro(
                "Returns an array containing all nodes in given graph of given type."),
            pathParameters(
                parameterWithName("graphId").description("Graph identifier (UUID)"),
                parameterWithName("typeId").description("Type identifier (matches `" + CODE + "`)")
            )))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetGraphNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-graph-nodes",
            operationIntro(
                "Returns an array containing all nodes in given graph."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .get("/api/graphs/{graphId}/nodes", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-all-nodes",
            operationIntro(
                "Returns an array containing all nodes visible to the user.")))
        .when()
        .get("/api/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveNode() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-node",
            operationIntro("On success, operation returns the saved node."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier"),
                parameterWithName("typeId")
                    .description("Type identifier")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of type objects is expected. Multiple node are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed.")),
            requestFields(
                fieldWithPath("id")
                    .description("Node identifier (UUID). Typically a random ID is generated if ID "
                        + "is not given."),
                subsectionWithPath("type")
                    .description("Node type identifier."),
                fieldWithPath("code")
                    .description("Optional identifying code for the node. Code must be unique over "
                        + "the node type. Code must match `" + CODE + "`. If code is not given, "
                        + "a default code is generated on insert."),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the node. Code must be unique over "
                        + "the graph. If URI is not given, a default URI is generated on insert."),
                subsectionWithPath("properties")
                    .description("Optional map of node properties. Keys are text attribute IDs "
                        + "defined by node's type. Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Node Name\" }`)"),
                subsectionWithPath("references")
                    .description("Optional map of node references. Keys are reference attribute "
                        + "IDs defined by node's type. Values are lists of node ID objects (e.g. "
                        + "`{ \"id\": \"...\","
                        + "   \"type\": { \"id\": \"Person\","
                        + "               \"graph\": { \"id\": \"...\" } } }`)"))))
        .when()
        .body(gson.toJson(exampleNode0))
        .post("/api/graphs/{graphId}/types/{typeId}/nodes",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveNodeUsingPut() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-node-using-put", operationIntro(
            "Saving using `PUT` is also supported. Node id is given as a path parameter.\n"
                + "On success, operation will return the saved node."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)"))))
        .when()
        .body(gson.toJson(exampleNode0))
        .put("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId(),
            exampleNode0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentDeleteNode() {
    given(adminAuthorizedRequest)
        .filter(document("delete-a-node", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A node can't be deleted if it's referred by another node."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier"))))
        .when()
        .delete("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
