package fi.thl.termed.web;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.ExampleData.anotherGraph;
import static fi.thl.termed.web.ExampleData.anotherGraphId;
import static fi.thl.termed.web.ExampleData.exampleGraph;
import static fi.thl.termed.web.ExampleData.exampleGraphId;
import static fi.thl.termed.web.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
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

public class GraphApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @Before
  public void insertExampleGraph() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @After
  public void deleteExampleGraph() {
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void documentGetGraphById() {
    given(adminAuthorizedJsonGetRequest).filter(
        document("get-a-graph",
            operationIntro(),
            pathParameters(
                parameterWithName("id")
                    .description("Graph identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("id")
                    .description("Graph identifier (UUID)."),
                fieldWithPath("code")
                    .description("Optional identifying code for the graph."),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the type."),
                fieldWithPath("roles")
                    .description("Optional list of roles defined for the graph. "
                        + "Roles are returned only for admin users."),
                subsectionWithPath("permissions")
                    .description("Optional map of graph permissions where keys are graph roles and "
                        + "values are lists of permissions. Permissions are returned only for "
                        + "admin users."),
                subsectionWithPath("properties")
                    .description("Optional map of graph properties where keys are property ids "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."))))
        .when()
        .get("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllGraphs() {
    given(adminAuthorizedJsonSaveRequest)
        .body(anotherGraph)
        .post("/api/graphs?mode=insert");

    given(adminAuthorizedJsonGetRequest).filter(
        document("get-all-graphs",
            operationIntro("Returns an array containing all graphs visible to the user. "
                + "Roles and permissions are visible for admin users only.")))
        .when()
        .get("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", anotherGraphId.getId());
  }

  @Test
  public void documentSaveGraph() {
    given(adminAuthorizedJsonSaveRequest).filter(
        document("save-a-graph",
            operationIntro("If posted object contains an id, a graph is either updated "
                + "or inserted with the given id. If id is not present, graph is saved with "
                + "new random id.\n\nOn success, operation returns the saved graph."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of graph objects is expected. Multiple graphs are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed.")),
            requestFields(
                fieldWithPath("id")
                    .description("Graph identifier (UUID). If an id is provided, existing graph is "
                        + "updated or a new one is created with given id. If id is not given, a new "
                        + "graph is created with random id."),
                fieldWithPath("code")
                    .description("Optional identifying code for the graph. Code must be "
                        + "unique and match pattern `" + CODE + "`"),
                fieldWithPath("uri")
                    .description("Optional identifying uri for the graph. URI must be unique."),
                fieldWithPath("roles")
                    .description("Optional list of roles defined for the graph. "
                        + "A role must match pattern `" + CODE + "`. "
                        + "Only application admins can update graph roles."),
                subsectionWithPath("permissions")
                    .description("Optional map of graph permissions. Keys are a graph roles and "
                        + "must be included in roles list. Values are lists of permissions where "
                        + "a permission is one of `INSERT`, `READ`, `UPDATE`, `DELETE`. "
                        + "Only application admins can update graph permissions."),
                subsectionWithPath("properties")
                    .description("Optional map of graph properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Graph\" }`)"))))
        .when()
        .body(exampleGraph)
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveGraphUsingPut() {
    given(adminAuthorizedJsonSaveRequest).filter(
        document("save-a-graph-using-put",
            operationIntro(
                "Saving using `PUT` is also supported. Graph id is given as a path parameter.\n"
                    + "On success, operation will return the saved graph."),
            pathParameters(parameterWithName("id").description("Graph identifier (UUID)"))))
        .when()
        .body(exampleGraph)
        .put("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentDeleteGraph() {
    given(adminAuthorizedRequest).filter(
        document("delete-a-graph",
            operationIntro(
                "On success, operation will return `204` with an empty body.\n\n"
                    + "A graph can't be deleted if it contains any data (types or nodes)."),
            pathParameters(parameterWithName("id").description("Graph identifier (UUID)"))))
        .when()
        .delete("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
