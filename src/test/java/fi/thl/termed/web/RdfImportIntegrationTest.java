package fi.thl.termed.web;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import fi.thl.termed.util.io.ResourceUtils;
import fi.thl.termed.util.json.JsonUtils;
import fi.thl.termed.util.rdf.JenaUtils;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

public class RdfImportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveRdfVocabulary() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-graph.json"))
        .when()
        .put("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save graph types
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(ResourceUtils.resourceToString("examples/nasa/example-types.json"))
        .when()
        .post("/api/graphs/" + graphId + "/types?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // save graph nodes
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/rdf+xml")
        .body(ResourceUtils.resourceToString("examples/nasa/example-nodes.rdf"))
        .when()
        .post("/api/graphs/" + graphId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldPickNodeIdFromUuidUrn() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save test graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-graph.json").toString())
        .when()
        .put("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save test types
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-types.json").toString())
        .when()
        .post("/api/graphs/" + graphId + "/types?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // id for node to be added
    String nodeId = UUID.randomUUID().toString();

    // check that node is not yet in the graph
    given()
        .auth().basic(testUsername, testPassword)
        .accept("application/json")
        .when().get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId)
        .then().statusCode(HttpStatus.SC_NOT_FOUND);

    Model model = ModelFactory.createDefaultModel();
    Resource subject = createResource("urn:uuid:" + nodeId);
    model.add(createStatement(subject, RDF.type, SKOS.Concept));
    model.add(createStatement(subject, SKOS.prefLabel, createPlainLiteral("Cat")));

    // save rdf model containing the node
    given()
        .auth().basic(testUsername, testPassword)
        .config(RestAssured.config().encoderConfig(
            encoderConfig().encodeContentTypeAs("application/rdf+xml", ContentType.XML)))
        .contentType("application/rdf+xml")
        .body(JenaUtils.toRdfXmlString(model))
        .when()
        .post("/api/graphs/" + graphId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // make sure that saved node exists
    given()
        .auth().basic(testUsername, testPassword)
        .accept("application/json")
        .when()
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId))
        .and()
        .body("properties.prefLabel[0].value", equalTo("Cat"));
  }

  @Test
  public void shouldSaveAndWaitForIndexing() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save test graph
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-graph.json").toString())
        .when()
        .put("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // save test types
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-types.json").toString())
        .when()
        .post("/api/graphs/" + graphId + "/types?batch=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // id for node to be added
    String nodeId = UUID.randomUUID().toString();

    // check that node is not yet in the graph
    given()
        .auth().basic(testUsername, testPassword)
        .accept("application/json")
        .when().get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId)
        .then().statusCode(HttpStatus.SC_NOT_FOUND);

    Model model = ModelFactory.createDefaultModel();
    Resource subject = createResource("urn:uuid:" + nodeId);
    model.add(createStatement(subject, RDF.type, SKOS.Concept));
    model.add(createStatement(subject, SKOS.prefLabel, createPlainLiteral("Cat")));

    // save rdf model containing the node
    // here we use stream parameter to enforce fresh index
    given()
        .auth().basic(testUsername, testPassword)
        .config(RestAssured.config().encoderConfig(
            encoderConfig().encodeContentTypeAs("application/rdf+xml", ContentType.XML)))
        .contentType("application/rdf+xml")
        .body(JenaUtils.toRdfXmlString(model))
        .when()
        .post("/api/graphs/" + graphId + "/nodes?stream=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // make sure that saved node can be found
    given()
        .auth().basic(testUsername, testPassword)
        .accept("application/json")
        .when()
        .get("/api/graphs/" + graphId + "/types/Concept/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", hasItems(nodeId));
  }

}
