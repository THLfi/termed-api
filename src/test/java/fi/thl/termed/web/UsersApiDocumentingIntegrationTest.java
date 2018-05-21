package fi.thl.termed.web;

import static fi.thl.termed.web.OperationIntroSnippet.operationIntro;
import static fi.thl.termed.web.TestExampleData.exampleGraph;
import static fi.thl.termed.web.TestExampleData.exampleGraphId;
import static fi.thl.termed.web.TestExampleData.exampleUser;
import static fi.thl.termed.web.TestExampleData.exampleUserName;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.cli.CliDocumentation.curlRequest;
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
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UsersApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @Before
  public void insertExampleGraphAndUser() {
    given(adminAuthorizedJsonSaveRequest)
        .body(gson.toJson(exampleGraph))
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(superuserAuthorizedRequest)
        .contentType("application/json")
        .body(gson.toJson(exampleUser))
        .post("/api/users?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @After
  public void deleteExampleGraphAndUser() {
    given(superuserAuthorizedRequest)
        .delete("/api/users/{id}", exampleUserName)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void documentGetUserByUsername() {
    given(superuserAuthorizedRequest)
        .accept("application/json")
        .filter(document("get-a-user",
            operationIntro("Get a user by username.\n\n"
                + "Request parameter based `GET /api/users?username=<username>` is also supported."),
            pathParameters(
                parameterWithName("username")
                    .description("Username identifying the user")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("username")
                    .description("Username identifying the user."),
                fieldWithPath("password")
                    .description("Password field is always empty in GET requests."),
                fieldWithPath("appRole")
                    .description(
                        "Application role for the user. Application role is one of `USER`, `ADMIN`, `SUPERUSER`."),
                subsectionWithPath("graphRoles")
                    .description(
                        "List of graph roles for the user. Each object in the list contains fields `graph` "
                            + "and `role` where graph is an ID object specifying the graph and `role` "
                            + "is some role in the graph."))))
        .when()
        .get("/api/users/{username}", exampleUserName)
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllUsers() {
    given(superuserAuthorizedRequest)
        .accept("application/json")
        .filter(documentationConfiguration(this.restDocumentation)
            .snippets().withDefaults(curlRequest()))
        .filter(document("get-all-users",
            operationIntro(
                "Returns an array containing all users.")))
        .when()
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveUser() {
    given(superuserAuthorizedRequest)
        .contentType("application/json")
        .filter(document("save-a-user",
            operationIntro("On success, operation returns `204`."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of type objects is expected. Multiple types are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed."),
                parameterWithName("updatePassword").optional()
                    .description("Optional boolean flag for skipping password update when updating "
                        + "an existing user (batch can't be `true` when skipping password update). "
                        + "If parameter is not specified, `true` is used, i.e. passwords are updated.")),
            requestFields(
                fieldWithPath("username")
                    .description("*Required.* Username identifying the user."),
                fieldWithPath("password")
                    .description("*Required.* Password for the user."),
                fieldWithPath("appRole")
                    .description("*Required.* Application role for the user. "
                        + "Application role is one of `USER`, `ADMIN`, `SUPERUSER`."),
                subsectionWithPath("graphRoles")
                    .description("Optional list of graph roles."))))
        .when()
        .body(gson.toJson(exampleUser))
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void documentDeleteUser() {
    given(superuserAuthorizedRequest)
        .filter(document("delete-a-user", operationIntro(
            "Request parameter based `DELETE /api/users?username=<username>` is also supported.\n\n"
                + "On success, operation will return `204` with an empty body.\n\n"
                + "User can't be deleted if it referenced from some node."),
            pathParameters(
                parameterWithName("username")
                    .description("Username identifying the user"))))
        .when()
        .delete("/api/users/{username}", exampleUserName)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
