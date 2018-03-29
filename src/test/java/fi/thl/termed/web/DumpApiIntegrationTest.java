package fi.thl.termed.web;

import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.gson.JsonObject;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class DumpApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldPostAndGetTrivialDump() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";
    String nodeId = UUID.randomUUID().toString();

    JsonObject graphIdObject = object("id", primitive(graphId));
    JsonObject typeIdObject = object("id", primitive(typeId), "graph", graphIdObject);
    JsonObject nodeIdObject = object("id", primitive(nodeId), "type", typeIdObject);

    JsonObject dump = object(
        "graphs", array(graphIdObject),
        "types", array(typeIdObject),
        "nodes", array(nodeIdObject));

    // save dump
    given(adminAuthorizedJsonRequest)
        .body(dump.toString())
        .post("/api/dump?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify dump (limit dump only to previously posted data)
    given(adminAuthorizedJsonRequest)
        .get("/api/dump?graphId=" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(APPLICATION_JSON_UTF8_VALUE)
        .body(sameJSONAs(dump.toString())
            .allowingExtraUnexpectedFields()
            .allowingAnyArrayOrdering());

    // double-check individually
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(graphIdObject.toString()).allowingExtraUnexpectedFields());
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(typeIdObject.toString()).allowingExtraUnexpectedFields());
    given(adminAuthorizedJsonRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(nodeIdObject.toString()).allowingExtraUnexpectedFields());

    // clean up
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedJsonRequest).delete("/api/graphs/" + graphId);
  }

}
