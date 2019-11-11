package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.util.NodesToCsv;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectField;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.request.RequestParametersSnippet;

class NodeCsvApiDocumentingIntegrationTest extends BaseNodeApiDocumentingIntegrationTest {

  private RequestParametersSnippet csvGetRequestParameters = requestParameters(
      parameterWithName("select").optional()
          .description("Optional list of fields that are selected to the result. Note that if "
              + "fields are selected, it might not be possible to import resulting output back."),
      parameterWithName("where").optional()
          .description("Optional query to specify which nodes are included."),
      parameterWithName("sort").optional()
          .description("Optional list of fields that determine the order."),
      parameterWithName("max").optional()
          .description("Optional numeric parameter to specify max results to return. "
              + "Default value is `-1`, i.e. all nodes are returned."),
      parameterWithName("delimiter").optional()
          .description("Optional parameter for CSV column delimiter. "
              + "Accepted values are `COMMA`, `SEMICOLON` and `TAB`. "
              + "Default value is `COMMA`."),
      parameterWithName("quoteChar").optional()
          .description("Optional parameter for CSV quote character. "
              + "Accepted values are `DOUBLE_QUOTE` and `SINGLE_QUOTE`. "
              + "Default value is `DOUBLE_QUOTE`."),
      parameterWithName("lineBreak").optional()
          .description("Optional parameter for CSV line break character. "
              + "Accepted values are `LF` and `CRLF`. "
              + "Default value is `LF`."),
      parameterWithName("quoteAll").optional()
          .description("Optional boolean parameter to enforce quoting. "
              + "Default value is `false`."),
      parameterWithName("charset").optional()
          .description("Optional parameter for response charset. "
              + "Default value is `UTF-8`."),
      parameterWithName("download").optional()
          .description("Optional boolean parameter to set content disposition header so "
              + "browser offer downloading. Default value is `true`."),
      parameterWithName("useLabeledReferences").optional()
          .description("Optional boolean parameter to tell serializer to use reference labels "
              + "in place of resource IDs. Default value is `false`. Note that if labeled references "
              + "are used and importing back is needed, labels should uniquely identify each node."),
      parameterWithName("labelAttribute").optional()
          .description("Optional parameter to tell which attribute should "
              + "be used for labeling. Requires that `useLabeledReferences=true`. Default "
              + "value is `prefLabel`."),
      parameterWithName("labelLang").optional()
          .description("Optional parameter to tell which lang should "
              + "be used for labeling. Requires that `useLabeledReferences=true`. Default "
              + "value is empty meaning any lang is accepted."));

  private RequestParametersSnippet csvPostRequestParameters = requestParameters(
      parameterWithName("delimiter").optional()
          .description("Optional parameter for CSV column delimiter. "
              + "Accepted values are `COMMA`, `SEMICOLON` and `TAB`. "
              + "Default value is `COMMA`."),
      parameterWithName("quoteChar").optional()
          .description("Optional parameter for CSV quote character. "
              + "Accepted values are `DOUBLE_QUOTE` and `SINGLE_QUOTE`. "
              + "Default value is `DOUBLE_QUOTE`."),
      parameterWithName("lineBreak").optional()
          .description("Optional parameter for CSV line break character. "
              + "Accepted values are `LF` and `CRLF`. "
              + "Default value is `LF`."),
      parameterWithName("quoteAll").optional()
          .description("Optional boolean parameter to enforce quoting. "
              + "Default value is `false`."),
      parameterWithName("charset").optional()
          .description("Optional parameter for response charset. "
              + "Default value is `UTF-8`."),
      parameterWithName("mode").optional()
          .description("Optional save mode. Supported modes are `insert`, `update`, "
              + "`upsert`. Default values is `upsert`."),
      parameterWithName("generateUris").optional()
          .description("Optional parameter to define whether missing URIs are "
              + "automatically generated. Default value is `false`."),
      parameterWithName("generateCodes").optional()
          .description("Optional parameter to define whether missing codes are "
              + "automatically generated. Default value is `false`."));

  @Test
  void documentGetTypeNodesInCsv() {
    given(adminAuthorizedRequest)
        .filter(document("get-type-nodes-in-csv",
            operationIntro("Get nodes of given type in CSV format. Request should have "
                + "header `Accept: text/csv`. Alternatively `.csv` suffix can be used in URL."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            csvGetRequestParameters))
        .accept(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .get(
            "/api/graphs/{graphId}/types/{typeId}/nodes?select=id,properties.name,references.knows",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetGraphNodesInCsv() {
    given(adminAuthorizedRequest)
        .filter(document("get-graph-nodes-in-csv",
            operationIntro("Get nodes of given graph in CSV format. Request should have "
                + "header `Accept: text/csv`. Alternatively `.csv` suffix can be used in URL."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            csvGetRequestParameters))
        .accept(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .get(
            "/api/graphs/{graphId}/nodes?select=id,p.name,r.knows&useLabeledReferences=true&labelAttribute=name",
            personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetNodesInCsv() {
    given(adminAuthorizedRequest)
        .filter(document("get-nodes-in-csv",
            operationIntro("Get all nodes in CSV format. Request should have "
                + "header `Accept: text/csv`. Alternatively `.csv` suffix can be used in URL."),
            csvGetRequestParameters))
        .accept(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .get(
            "/api/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentSaveTypeNodesInCsv() throws UnsupportedEncodingException {
    given(adminAuthorizedRequest)
        .filter(document("save-type-nodes-in-csv",
            operationIntro("Save nodes of given type in CSV format. Request should have "
                + "header `Content-type: text/csv`. "
                + "On success, operation will return `204` with an empty body."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            csvPostRequestParameters))
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .body(nodesAsCsv(
            ImmutableList.of(exampleNode0, exampleNode1),
            ImmutableList.of(new SelectId(),
                new SelectProperty("name"),
                new SelectReference("knows"))))
        .post("/api/graphs/{graphId}/types/{typeId}/nodes",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentSaveGraphNodesInCsv() {
    given(adminAuthorizedRequest)
        .filter(document("save-graph-nodes-in-csv",
            operationIntro("Save nodes of given graph in CSV format. Request should have "
                + "header `Content-type: text/csv`. "
                + "On success, operation will return `204` with an empty body."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            csvPostRequestParameters))
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .body(nodesAsCsv(
            ImmutableList.of(exampleNode0, exampleNode1),
            ImmutableList.of(new SelectId(), new SelectType(),
                new SelectProperty("name"),
                new SelectReference("knows"))))
        .post("/api/graphs/{graphId}/nodes",
            personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentSaveNodesInCsv() {
    given(adminAuthorizedRequest)
        .filter(document("save-nodes-in-csv",
            operationIntro("Save nodes in CSV format. Request should have "
                + "header `Content-type: text/csv`. "
                + "On success, operation will return `204` with an empty body."),
            csvPostRequestParameters))
        .contentType(MediaTypes.TEXT_CSV_VALUE)
        .when()
        .body(nodesAsCsv(
            ImmutableList.of(exampleNode0, exampleNode1),
            ImmutableList.of(new SelectId(),
                new SelectType(),
                new SelectField("code"),
                new SelectField("uri"),
                new SelectProperty("name"),
                new SelectReference("knows"))))
        .post("/api/nodes")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  private String nodesAsCsv(List<Node> nodes, List<Select> selects) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    NodesToCsv nodesToCsv = new NodesToCsv();
    nodesToCsv.writeAsCsv(
        nodes.stream(),
        selects,
        CsvOptions.builder().build(),
        byteArrayOutputStream);
    try {
      return byteArrayOutputStream.toString(Charsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }

}
