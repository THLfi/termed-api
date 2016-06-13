package fi.thl.termed.web;

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import fi.thl.termed.util.JsonUtils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class SchemeApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveTrivialScheme() {
    String schemeId = UUID.randomUUID().toString();

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body("{'id':'" + schemeId + "'}")
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));
  }

  @Test
  public void shouldSaveAndGetScheme() throws IOException {
    String schemeId = UUID.randomUUID().toString();

    JsonObject skosScheme = JsonUtils.getJsonResource("examples/skos/skos.json").getAsJsonObject();
    skosScheme.addProperty("id", schemeId);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(skosScheme.toString())
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // get the persisted scheme and compare to original allowing extra fields such as timestamps
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when()
        .get("/api/schemes/" + schemeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(skosScheme.toString()).allowingExtraUnexpectedFields());
  }

}
