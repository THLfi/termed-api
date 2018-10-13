package fi.thl.termed.web;

import static fi.thl.termed.web.ApiExampleData.exampleGraph;
import static fi.thl.termed.web.ApiExampleData.exampleGraphId;
import static fi.thl.termed.web.ApiExampleData.exampleNode0;
import static fi.thl.termed.web.ApiExampleData.exampleNode1;
import static fi.thl.termed.web.ApiExampleData.personType;
import static fi.thl.termed.web.ApiExampleData.personTypeId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;

import com.opencsv.CSVWriter;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class NodeCsvApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldGetTrivialNodeInCsv() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";
    String nodeId = UUID.randomUUID().toString();

    // save test data
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + nodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes?mode=insert");

    // get node data in csv
    given(adminAuthorizedRequest)
        .get("/api/graphs/" + graphId + "/types/" + typeId + "/nodes.csv")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .body(not(isEmptyString()));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldSaveNodesFromCsv() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonSaveRequest)
        .body(personType)
        .post("/api/graphs/" + exampleGraphId.getId() + "/types?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    String[] headers = {"id", "code", "p.name", "r.knows"};
    String[] row1 = {
        exampleNode0.getId().toString(),
        exampleNode0.getCode().orElse(null),
        exampleNode0.getFirstPropertyValue("name")
            .map(StrictLangValue::getValue).orElse(null),
        exampleNode0.getFirstReferenceValue("knows")
            .map(NodeId::getId).map(Objects::toString).orElse(null)};
    String[] row2 = {
        exampleNode1.getId().toString(),
        exampleNode1.getCode().orElse(null),
        exampleNode1.getFirstPropertyValue("name")
            .map(StrictLangValue::getValue).orElse(null)};

    StringWriter writer = new StringWriter();
    new CSVWriter(writer)
        .writeAll(Arrays.asList(headers, row1, row2));

    // post csv data
    given(adminAuthorizedRequest)
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .body(writer.toString())
        .post("/api/graphs/{graphId}/types/{typeId}/nodes?mode=insert",
            exampleGraphId.getId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleGraphId.getId(), personType.getId(), exampleNode0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("code",
            equalTo(exampleNode0.getCode().orElse(null)))
        .body("properties.name[0].value",
            equalTo(exampleNode0
                .getFirstPropertyValue("name")
                .map(StrictLangValue::getValue)
                .orElse(null)))
        .body("references.knows[0].id",
            equalTo(exampleNode0
                .getFirstReferenceValue("knows")
                .map(NodeId::getId)
                .map(Objects::toString)
                .orElse(null)));

    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleGraphId.getId(), personType.getId(), exampleNode1.getId())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("code",
            equalTo(exampleNode1.getCode().orElse(null)))
        .body("properties.name[0].value",
            equalTo(exampleNode1
                .getFirstPropertyValue("name")
                .map(StrictLangValue::getValue)
                .orElse(null)));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/nodes", exampleGraph.getId());
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}/types", exampleGraph.getId());
    given(adminAuthorizedRequest).delete("/api/graphs/{graphId}", exampleGraph.getId());
  }

}
