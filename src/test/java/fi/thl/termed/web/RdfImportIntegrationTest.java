package fi.thl.termed.web;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import fi.thl.termed.util.io.ResourceUtils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class RdfImportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveRdfVocabulary() throws IOException {
    String schemeId = UUID.randomUUID().toString();

    // save scheme
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-scheme.json"))
        .when()
        .put("/api/schemes/" + schemeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save scheme classes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-classes.json"))
        .when()
        .post("/api/schemes/" + schemeId + "/classes?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // save scheme resources
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/rdf+xml")
        .body(ResourceUtils.resourceToString("examples/nasa/example-resources.rdf"))
        .when()
        .post("/api/schemes/" + schemeId + "/resources")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
