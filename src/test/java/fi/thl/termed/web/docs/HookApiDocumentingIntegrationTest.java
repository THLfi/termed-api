package fi.thl.termed.web.docs;

import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import fi.thl.termed.domain.Webhook;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HookApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  private Webhook exampleHook0 = new Webhook("http://example.org/notificationReceiver");
  private Webhook exampleHook1 = new Webhook("http://foo.org/api/events");

  @BeforeEach
  void insertExampleHooks() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleHook0)
        .post("/api/hooks")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(exampleHook1)
        .post("/api/hooks")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @AfterEach
  void deleteExampleHooks() {
    given(superuserAuthorizedRequest)
        .delete("/api/hooks/{id}", exampleHook0.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedRequest)
        .delete("/api/hooks/{id}", exampleHook1.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentGetAllHooks() {
    given(adminAuthorizedRequest)
        .accept("application/json")
        .filter(document("get-all-webhooks",
            operationIntro("Returns an array containing all hooks.")))
        .when()
        .get("/api/hooks")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetHookById() {
    given(superuserAuthorizedRequest)
        .accept("application/json")
        .filter(document("get-a-webhook",
            operationIntro("Get a hook by ID."),
            pathParameters(
                parameterWithName("id")
                    .description("Web hook identifier (UUID).")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("id")
                    .description("Web hook identifier (UUID)."),
                fieldWithPath("url")
                    .description("Web hook URL."))))
        .when()
        .get("/api/hooks/{id}", exampleHook0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentSaveHook() {
    given(superuserAuthorizedRequest)
        .contentType("application/json")
        .filter(document("save-a-webhook",
            operationIntro("Create or update a hook. Returns the new or updated hook."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("url")
                    .optional()
                    .description("Optional parameter for giving hook URL as request parameter. "
                        + "If `url` request parameter is given, 1) request body is ignored, "
                        + "and 2) new hook ID is returned as text.")),
            requestFields(
                fieldWithPath("id")
                    .description("Web hook identifier (UUID)."),
                fieldWithPath("url")
                    .description("Web hook URL."))))
        .when()
        .body(exampleHook0)
        .post("/api/hooks")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentDeleteAllHooks() {
    given(superuserAuthorizedRequest)
        .filter(document("delete-all-webhooks", operationIntro(
            "Delete all hooks. On success, operation will return `204` with an empty body.")))
        .when()
        .delete("/api/hooks")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentDeleteHookById() {
    given(superuserAuthorizedRequest)
        .filter(document("delete-a-webhook", operationIntro(
            "Delete hook by ID. On success, operation will return `204` with an empty body."),
            pathParameters(
                parameterWithName("id")
                    .description("Web hook identifier (UUID)."))))
        .when()
        .delete("/api/hooks/{id}", exampleHook0.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
