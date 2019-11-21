package fi.thl.termed.web.docs;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode0Id;
import static fi.thl.termed.web.docs.DocsExampleData.exampleNode1;
import static fi.thl.termed.web.docs.DocsExampleData.personTypeId;
import static fi.thl.termed.web.docs.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Node;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeApiDocumentingIntegrationTest extends BaseNodeApiDocumentingIntegrationTest {

  @Test
  void documentGetNodeById() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-node",
            operationIntro("Get a node by id in given graph."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("id")
                    .description("Node identifier (UUID)"),
                subsectionWithPath("type")
                    .description("Type identifier object"),
                fieldWithPath("code")
                    .description("Optional identifying code for the node"),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the node"),
                fieldWithPath("number")
                    .description("Node number"),
                fieldWithPath("createdBy")
                    .description("Creator of the node (username)"),
                fieldWithPath("createdDate")
                    .description("Node created date in ISO format"),
                fieldWithPath("lastModifiedBy")
                    .description("Last modifier of the node (username)"),
                fieldWithPath("lastModifiedDate")
                    .description("Node last modified date in ISO format"),
                subsectionWithPath("properties")
                    .description("Optional map of node properties where keys are attribute IDs "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."),
                subsectionWithPath("references")
                    .description("Optional map of node references where keys are attribute IDs "
                        + "(e.g. `knows`) and values are a lists of node ids."),
                subsectionWithPath("referrers")
                    .description("Optional map of node referrers with same structure as "
                        + "references."))))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetTypeNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-type-nodes",
            operationIntro(
                "Returns an array containing all nodes in given graph of given type."),
            pathParameters(
                parameterWithName("graphId").description("Graph identifier (UUID)"),
                parameterWithName("typeId").description("Type identifier (matches `" + CODE + "`)")
            )))
        .when()
        .get("/api/graphs/{graphId}/types/{typeId}/nodes",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetGraphNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-graph-nodes",
            operationIntro(
                "Returns an array containing all nodes in given graph."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .get("/api/graphs/{graphId}/nodes", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentGetAllNodes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-all-nodes",
            operationIntro(
                "Returns an array containing all nodes visible to the user.")))
        .when()
        .get("/api/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentSaveNode() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-node",
            operationIntro("Note that `POST` is also supported for paths: "
                + "`/api/graphs/{graphId}/nodes` and `/api/nodes`.\n\n"
                + "On success, operation returns the saved node, for batch operations an empty "
                + "body with status `204` is returned."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier"),
                parameterWithName("typeId")
                    .description("Type identifier")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of type objects is expected. Multiple node are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed."),
                parameterWithName("generateUris").optional()
                    .description("Optional parameter to define whether missing URIs are "
                        + "automatically generated. Default value is `true`."),
                parameterWithName("uriNamespace").optional()
                    .description("Optional parameter to give URI namespace for generated URIs. "
                        + "By default Graph URI is used, and if not present, configuration property "
                        + "`fi.thl.termed.defaultNamespace` is used."),
                parameterWithName("generateCodes").optional()
                    .description("Optional parameter to define whether missing codes are "
                        + "automatically generated. Default value is `true`.")),
            requestFields(
                fieldWithPath("id")
                    .description("Node identifier (UUID). Typically a random ID is generated if ID "
                        + "is not given."),
                subsectionWithPath("type")
                    .description("Node type identifier."),
                fieldWithPath("code")
                    .description("Optional identifying code for the node. Code must be unique over "
                        + "the node type. Code must match `" + CODE + "`. If code is not given, "
                        + "a default code is generated on insert."),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the node. Code must be unique over "
                        + "the graph. If URI is not given, a default URI is generated on insert."),
                subsectionWithPath("properties")
                    .description("Optional map of node properties. Keys are text attribute IDs "
                        + "defined by node's type. Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Node Name\" }`)"),
                subsectionWithPath("references")
                    .description("Optional map of node references. Keys are reference attribute "
                        + "IDs defined by node's type. Values are lists of node ID objects (e.g. "
                        + "`{ \"id\": \"...\","
                        + "   \"type\": { \"id\": \"Person\","
                        + "               \"graph\": { \"id\": \"...\" } } }`)"))))
        .when()
        .body(exampleNode0)
        .post("/api/graphs/{graphId}/types/{typeId}/nodes",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentSaveNodeUsingPut() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-node-using-put", operationIntro(
            "Saving using `PUT` is also supported. Node id is given as a path parameter.\n"
                + "On success, operation will return the saved node."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)"))))
        .when()
        .body(exampleNode0)
        .put("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId(),
            exampleNode0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentUpdateNodeUsingPatch() {
    Node examplePatch = Node.builder()
        .id(exampleNode0.identifier())
        .addProperty("name", "Johnny")
        .build();

    given(adminAuthorizedJsonSaveRequest)
        .filter(document("update-a-node-using-patch", operationIntro(
            "Partially updating using `PATCH` is also supported. "
                + "Node id is given as a path parameter.\n"
                + "On success, operation will return the patched node."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier (UUID)")),
            requestParameters(
                parameterWithName("append").optional()
                    .description("If append is `true`, new property (or reference) values are "
                        + "appended to value arrays. Otherwise given values for given property "
                        + "replace the previous values. Default value is `true` (due to original "
                        + "behaviour of the patch method)."))))
        .when()
        .body(examplePatch)
        .patch("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId(),
            exampleNode0.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void documentUpdateTypeNodesUsingPatch() {
    List<Node> examplePatches = ImmutableList.of(
        Node.builder()
            .id(exampleNode0.identifier())
            .addProperty("name", "Johnny")
            .build(),
        Node.builder()
            .id(exampleNode1.identifier())
            .addProperty("name", "Janie")
            .build());

    given(adminAuthorizedJsonSaveRequest)
        .filter(document("update-type-nodes-using-patch", operationIntro(
            "Patch multiple nodes of same type. Request body contains an array of"
                + " values where each must have an ID. On success, returns `204`."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestParameters(
                parameterWithName("batch")
                    .description("Currently, this must be `true`. Single value can be patched by "
                        + "calling `PATCH` on full node identifying `URL`."),
                parameterWithName("append").optional()
                    .description("If append is `true`, new property (or reference) values are "
                        + "appended to value arrays. Otherwise given values for given property "
                        + "replace the previous values. Default value is `true` (due to original "
                        + "behaviour of the patch method)."),
                parameterWithName("lenient").optional()
                    .description("If lenient is `true`, patching does not fail if target node is "
                        + "missing. Default value is `false`."))))
        .when()
        .body(examplePatches)
        .patch("/api/graphs/{graphId}/types/{typeId}/nodes?batch=true",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentUpdateSpecificNodesUsingPatch() {
    Node examplePatch = Node.builder()
        .id(exampleNode0.identifier())
        .addProperty("name", "Patched")
        .build();

    given(adminAuthorizedJsonSaveRequest)
        .filter(document("update-specific-nodes-using-patch", operationIntro(
            "Patch multiple nodes of same type specified by `where`. Request body contains "
                + "a single node that is applied against all matching nodes. Possible ID of the "
                + "given node is ignored. On success, returns `204`."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestParameters(
                parameterWithName("where")
                    .description("Required query to specify which nodes should be patched. "
                        + "Empty where means that all nodes (of type) should be updated."),
                parameterWithName("append").optional()
                    .description("If append is `true`, new property (or reference) values are "
                        + "appended to value arrays. Otherwise given values for given property "
                        + "replace the previous values. Default value is `true` (due to original "
                        + "behaviour of the patch method)."))))
        .when()
        .body(examplePatch)
        .patch("/api/graphs/{graphId}/types/{typeId}/nodes?where=properties.name:J*",
            exampleNode0.getTypeGraphId(),
            exampleNode0.getTypeId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentDeleteNode() {
    given(adminAuthorizedRequest)
        .filter(document("delete-a-node", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A node can't be deleted if it's referred by another node unless disconnect "
                + "is set true."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)"),
                parameterWithName("id")
                    .description("Node identifier")),
            requestParameters(
                parameterWithName("disconnect").optional()
                    .description("If disconnect is `true`, remove all references to this node "
                        + "before deleting. Default value is `false`."))))
        .when()
        .delete("/api/graphs/{graphId}/types/{typeId}/nodes/{id}",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId(),
            exampleNode0Id.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentNodesById() {
    given(adminAuthorizedRequest)
        .filter(document("delete-nodes-by-id", operationIntro(
            "Deletes node by ID or IDs given in request body.\n\n"
                + "On success, operation will return `204` with an empty body.\n\n"
                + "A node can't be deleted if it's referred by another node unless disconnect "
                + "is set true. Note that body in delete request might not be supported on all "
                + "clients."),
            requestParameters(
                parameterWithName("batch").optional()
                    .description("If batch is `true`, list of node IDs is expected in "
                        + "request body. If batch is `false` one node ID is expected in body. "
                        + "Default value is `false`."),
                parameterWithName("disconnect").optional()
                    .description("If disconnect is `true`, remove all references to this node "
                        + "before deleting. If graph does not reference other graphs, disconnect is "
                        + "not needed. Default value is `false`."))))
        .when()
        .body(exampleNode0Id)
        .delete("/api/nodes")
        .then()
        // rest assured ignores body payload on delete and api does not accept an empty body here
        // so bad request is expected.
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void documentDeleteTypeNodes() {
    given(adminAuthorizedRequest)
        .filter(document("delete-type-nodes", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A node can't be deleted if it's referred by another node unless disconnect "
                + "is set true."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("typeId")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestParameters(
                parameterWithName("batch").optional()
                    .description("If batch is `true`, list of node IDs is expected in "
                        + "request body. If batch is `false` all instances of type are deleted. "
                        + "Default value is `false`."),
                parameterWithName("disconnect").optional()
                    .description("If disconnect is `true`, remove all references to this node "
                        + "before deleting. Default value is `false`."))))
        .when()
        .delete("/api/graphs/{graphId}/types/{typeId}/nodes",
            exampleNode0Id.getTypeGraphId(),
            exampleNode0Id.getTypeId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void documentGraphTypeNodes() {
    given(adminAuthorizedRequest)
        .filter(document("delete-graph-nodes", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A node can't be deleted if it's referred by another node unless disconnect "
                + "is set true."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            requestParameters(
                parameterWithName("batch").optional()
                    .description("If batch is `true`, list of node IDs is expected in "
                        + "request body. If batch is `false` all nodes in graph are deleted. "
                        + "Default value is `false`."),
                parameterWithName("disconnect").optional()
                    .description("If disconnect is `true`, remove all references to this node "
                        + "before deleting. If graph does not reference other graphs, disconnect is "
                        + "not needed. Default value is `false`."))))
        .when()
        .delete("/api/graphs/{graphId}/nodes",
            exampleNode0Id.getTypeGraphId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
