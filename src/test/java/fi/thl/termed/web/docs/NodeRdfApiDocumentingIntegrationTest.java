package fi.thl.termed.web.docs;

import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.rdf.JenaUtils;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

class NodeRdfApiDocumentingIntegrationTest extends BaseNodeApiDocumentingIntegrationTest {

  @Test
  void documentGetNodesInRdf() {
    given(adminAuthorizedRequest)
        .filter(document("get-graph-nodes-in-rdf",
            operationIntro(
                "Returns a stream of RDF data. Stream contain all triples in the graph. "
                    + "Supported formats are `text/turtle` and `application/n-triples`."),
            requestParameters(
                parameterWithName("download").optional()
                    .description("Optional boolean parameter to specify whether content "
                        + "disposition header is set to trigger download. "
                        + "Default value is `false`.")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .accept(RdfMediaTypes.TURTLE_VALUE)
        .get("/api/graphs/{graphId}/nodes", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetNodesInRdfBySparql() {
    String exampleSparqlQuery =
        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "SELECT *\n"
            + "WHERE {\n"
            + "  ?s ?p ?o .\n"
            + "}\n"
            + "LIMIT 25";

    given(adminAuthorizedRequest)
        .filter(document("get-graph-nodes-in-rdf-by-sparql",
            operationIntro(
                "Returns a CSV table corresponding to given SPARQL query. In background Termed "
                    + "index is used as a triple store. Performance might not be on par with "
                    + "native triple stores. Supported output formats are `text/csv` and "
                    + "`text/plain`. Plain text should be used only for small result sets as it's "
                    + "slower to render."),
            requestParameters(
                parameterWithName("timeout").optional()
                    .description("Optional integer parameter to specify query timeout in seconds. "
                        + "Default value is `10`."),
                parameterWithName("postProcess").optional()
                    .description("Optional boolean parameter for applying SPARQL updates after "
                        + "first exporting the whole graph. This means that update is not actually "
                        + "applied to the persisted graph, only to in-memory export. SPARQL post "
                        + "processing should be done to relatively small graphs only.\n\n"
                        + "Parameter `timeout` is not supported with `postProcess`. Default value "
                        + "for `postProcess` is `false`.")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .accept("text/csv")
        .body(exampleSparqlQuery)
        .post("/api/graphs/{graphId}/nodes/sparql",
            personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGraphSaveNodesInRdf() {
    Model rdfModel = ModelFactory.createDefaultModel();

    Resource exampleNode0Resource = createResource(exampleNode0.getUri().orElse(null));
    Resource exampleNode1Resource = createResource(exampleNode1.getUri().orElse(null));

    rdfModel.add(exampleNode0Resource, RDF.type, FOAF.Person);
    rdfModel.add(exampleNode0Resource, FOAF.name,
        exampleNode0.getFirstPropertyValue("name")
            .map(StrictLangValue::getValue).orElse(""));
    rdfModel.add(exampleNode0Resource, FOAF.mbox,
        exampleNode0.getFirstPropertyValue("email")
            .map(StrictLangValue::getValue).orElse(""));
    rdfModel.add(exampleNode0Resource, FOAF.knows, exampleNode1Resource);

    rdfModel.add(exampleNode1Resource, RDF.type, FOAF.Person);
    rdfModel.add(exampleNode1Resource, FOAF.name,
        exampleNode1.getFirstPropertyValue("name")
            .map(StrictLangValue::getValue).orElse(""));
    rdfModel.add(exampleNode1Resource, FOAF.mbox,
        exampleNode1.getFirstPropertyValue("email")
            .map(StrictLangValue::getValue).orElse(""));

    given(adminAuthorizedRequest)
        .filter(document("save-graph-nodes-in-rdf",
            operationIntro("Save RDF nodes into given graph."),
            requestParameters(
                parameterWithName("importCodes").optional()
                    .description("Optional parameter to define whether Node code values are parsed "
                        + "and imported from URI local names. Default value is `true`."),
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. Default values is `upsert`."),
                parameterWithName("generateUris").optional()
                    .description("Optional parameter to define whether missing URIs are "
                        + "automatically generated. Default value is `false`."),
                parameterWithName("generateCodes").optional()
                    .description("Optional parameter to define whether missing codes are "
                        + "automatically generated. Default value is `false`.")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .contentType(RdfMediaTypes.TURTLE_VALUE)
        .body(JenaUtils.toRdfTtlString(rdfModel))
        .post("/api/graphs/{graphId}/nodes", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
