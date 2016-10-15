package fi.thl.termed.web;

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import fi.thl.termed.util.JsonUtils;
import fi.thl.termed.util.ResourceUtils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class RdfImportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveRdfVocabulary() throws IOException {
    String schemeId = UUID.randomUUID().toString();

    JsonObject skosScheme = JsonUtils.getJsonResource("examples/nasa/skos.json").getAsJsonObject();
    skosScheme.addProperty("id", schemeId);

    String exampleVocabulary = ResourceUtils.getResourceToString("examples/nasa/access.skos");

    // save a new skos meta scheme
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(skosScheme.toString())
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save the rdf skos vocabulary, saves only properties defined on skos.json
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/rdf+xml")
        .body(exampleVocabulary)
        .when()
        .post("/api/schemes/" + schemeId + "/resources")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
