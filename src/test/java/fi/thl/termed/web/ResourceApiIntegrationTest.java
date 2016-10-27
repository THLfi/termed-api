package fi.thl.termed.web;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ResourceApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveAndGetTrivialResource() {
    String schemeId = UUID.randomUUID().toString();
    String classId = "Concept";
    String resourceId = UUID.randomUUID().toString();

    // save scheme
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + schemeId + "'}")
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save class
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + classId + "'}")
        .when()
        .post("/api/schemes/" + schemeId + "/classes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(classId));

    // save one resource
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + resourceId + "'}")
        .when()
        .post("/api/schemes/" + schemeId + "/classes/" + classId + "/resources")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(resourceId));

    // get one resource
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/schemes/" + schemeId + "/classes/" + classId + "/resources/" + resourceId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(resourceId));
  }

  @Test
  public void shouldSaveAndGetSimpleVocabulary() throws IOException {
    String schemeId = UUID.randomUUID().toString();

    // save scheme
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-scheme.json"))
        .when()
        .put("/api/schemes/" + schemeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save classes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-classes.json"))
        .when()
        .post("/api/schemes/" + schemeId + "/classes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // save resources
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(resourceToString("examples/termed/animals-resources.json"))
        .when()
        .post("/api/schemes/" + schemeId + "/classes/Concept/resources?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // check that we get the same vocabulary information back (bypass index as its built async)
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/schemes/" + schemeId + "/classes/Concept/resources?bypassIndex=true")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(resourceToString("examples/termed/animals-resources.json"))
                  .allowingExtraUnexpectedFields()
                  .allowingAnyArrayOrdering());
  }

}
