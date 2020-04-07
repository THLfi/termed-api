package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fi.thl.termed.domain.DefaultUris;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.rdf.JenaUtils;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import java.util.Arrays;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

class RdfExportIntegrationTest extends BaseApiIntegrationTest {

  @Value("${fi.thl.termed.defaultNamespace:}")
  private String defaultNamespace;

  @Test
  void shouldExportRdfForNodesWithoutGivenUris() {
    UUID graphId = UUID.randomUUID();

    // save graph and types
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-graph.json"))
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body(resourceToString("examples/termed/animals-types.json"))
        .post("/api/graphs/" + graphId + "/types?batch=true");

    Node animalNode = Node.builder()
        .random(TypeId.of("Concept", graphId))
        .addProperty("prefLabel", "Animal")
        .build();

    Node dogNode = Node.builder()
        .random(TypeId.of("Concept", graphId))
        .addProperty("prefLabel", "Dog")
        .addReference("broader", animalNode.identifier())
        .build();

    // save a few nodes
    given(adminAuthorizedJsonSaveRequest)
        .body(Arrays.asList(animalNode, dogNode))
        .post("/api/graphs/" + graphId + "/nodes?batch=true&generateCodes=false&generateUris=false")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify that nodes got saved
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/node-count")
        .then()
        .body(equalTo("2"));

    String rdfResponse = given(adminAuthorizedRequest)
        .accept(RdfMediaTypes.TURTLE_VALUE)
        .get("/api/graphs/" + graphId + "/nodes")
        .thenReturn()
        .getBody()
        .prettyPrint();

    Model rdfModel = JenaUtils.fromRdfTtlString(rdfResponse);

    assertTrue(rdfModel.contains(
        ResourceFactory.createResource(
            DefaultUris.uri(defaultNamespace, animalNode.identifier())),
        SKOS.prefLabel,
        "Animal"));
    assertTrue(rdfModel.contains(
        ResourceFactory.createResource(
            DefaultUris.uri(defaultNamespace, dogNode.identifier())),
        SKOS.prefLabel,
        "Dog"));
    assertTrue(rdfModel.contains(
        ResourceFactory.createResource(
            DefaultUris.uri(defaultNamespace, dogNode.identifier())),
        SKOS.broader,
        ResourceFactory.createResource(
            DefaultUris.uri(defaultNamespace, animalNode.identifier()))));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

}
