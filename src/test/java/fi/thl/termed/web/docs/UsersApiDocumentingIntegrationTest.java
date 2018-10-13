package fi.thl.termed.web.docs;

import static fi.thl.termed.web.docs.DocsExampleData.exampleGraph;
import static fi.thl.termed.web.docs.DocsExampleData.exampleGraphId;
import static fi.thl.termed.web.docs.DocsExampleData.exampleUser;
import static fi.thl.termed.web.docs.DocsExampleData.exampleUserName;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsersApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @BeforeEach
  void insertExampleGraphAndUser() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(superuserAuthorizedRequest)
        .contentType("application/json")
        .body(exampleUser)
        .post("/api/users?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @AfterEach
  void deleteExampleGraphAndUser() {
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
  void documentGetUserByUsername() {
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
  void documentGetAllUsers() {
    given(adminAuthorizedRequest)
        .accept("application/json")
        .filter(document("get-all-users",
            operationIntro(
                "Returns an array containing all users.")))
        .when()
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentSaveUser() {
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
        .body(exampleUser)
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentDeleteUser() {
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
