package fi.thl.termed.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static fi.thl.termed.util.JsonUtils.getJsonResource;
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
        .auth().basic(username, password)
        .contentType("application/json")
        .body("{'id':'" + schemeId + "','classes':[{'id':'" + classId + "'}]}")
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save one resource
    given()
        .auth().basic(username, password)
        .contentType("application/json")
        .body("{'id':'" + resourceId + "'}")
        .when()
        .post("/api/schemes/" + schemeId + "/classes/" + classId + "/resources")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(resourceId));

    // get one resource
    given()
        .auth().basic(username, password)
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

    JsonObject scheme = getJsonResource("examples/termed/animals-scheme.json").getAsJsonObject();
    scheme.addProperty("id", schemeId);

    JsonElement data = getJsonResource("examples/termed/animals-data.json");

    // save scheme
    given()
        .auth().basic(username, password)
        .contentType("application/json")
        .body(scheme.toString())
        .when()
        .post("/api/schemes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(schemeId));

    // save vocabulary data
    given()
        .auth().basic(username, password)
        .contentType("application/json")
        .body(data.toString())
        .when()
        .post("/api/schemes/" + schemeId + "/classes/Concept/resources?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // check that we get the same vocabulary information back (bypass index as its built async)
    given()
        .auth().basic(username, password)
        .contentType("application/json")
        .when()
        .get("/api/schemes/" + schemeId + "/classes/Concept/resources?bypassIndex=true")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(data.toString())
                  .allowingExtraUnexpectedFields()
                  .allowingAnyArrayOrdering());
  }

}
