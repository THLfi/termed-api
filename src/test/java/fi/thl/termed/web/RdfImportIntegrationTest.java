package fi.thl.termed.web;

import static io.restassured.RestAssured.given;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.hamcrest.core.IsEqual.equalTo;

import fi.thl.termed.util.io.ResourceUtils;
import fi.thl.termed.util.json.JsonUtils;
import fi.thl.termed.util.rdf.JenaUtils;
import java.io.IOException;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;

class RdfImportIntegrationTest extends BaseApiIntegrationTest {

  @Test
  void shouldSaveRdfVocabulary() {
    String graphId = UUID.randomUUID().toString();

    // save graph and types
    given(adminAuthorizedJsonSaveRequest)
        .body(ResourceUtils.resourceToString("examples/nasa/example-graph.json"))
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body(ResourceUtils.resourceToString("examples/nasa/example-types.json"))
        .post("/api/graphs/" + graphId + "/types?batch=true");

    // save nodes
    given(adminAuthorizedRequest)
        .contentType("application/rdf+xml")
        .body(ResourceUtils.resourceToString("examples/nasa/example-nodes.rdf"))
        .post("/api/graphs/" + graphId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify that nodes got saved
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/node-count")
        .then()
        .body(equalTo("16"));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  void shouldPickNodeIdFromUuidUrn() throws IOException {
    String graphId = UUID.randomUUID().toString();

    // save test graph and types
    given(adminAuthorizedJsonSaveRequest)
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-graph.json").toString())
        .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body(JsonUtils.getJsonResource("examples/skos/example-skos-types.json").toString())
        .post("/api/graphs/" + graphId + "/types?batch=true");

    // id for node to be added
    String nodeId = UUID.randomUUID().toString();

    // check that node is not yet in the graph
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    Model model = ModelFactory.createDefaultModel();
    Resource subject = createResource("urn:uuid:" + nodeId);
    model.add(createStatement(subject, RDF.type, SKOS.Concept));
    model.add(createStatement(subject, SKOS.prefLabel, createPlainLiteral("Cat")));

    // save rdf model containing the node
    given(adminAuthorizedRequest)
        .contentType("application/rdf+xml")
        .body(JenaUtils.toRdfXmlString(model))
        .post("/api/graphs/" + graphId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // make sure that saved node exists
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + nodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(nodeId))
        .body("properties.prefLabel[0].value", equalTo("Cat"));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

}
