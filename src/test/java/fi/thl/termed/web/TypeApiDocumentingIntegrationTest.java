package fi.thl.termed.web;

import static fi.thl.termed.util.RegularExpressions.ALL;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.web.ExampleData.exampleGraph;
import static fi.thl.termed.web.ExampleData.exampleGraphId;
import static fi.thl.termed.web.ExampleData.groupType;
import static fi.thl.termed.web.ExampleData.personType;
import static fi.thl.termed.web.ExampleData.personTypeId;
import static fi.thl.termed.web.OperationIntroSnippet.operationIntro;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.snippet.Attributes.key;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TypeApiDocumentingIntegrationTest extends BaseApiDocumentingIntegrationTest {

  @Before
  public void insertExampleGraphWithTypes() {
    given(adminAuthorizedJsonSaveRequest)
        .body(exampleGraph)
        .post("/api/graphs?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(adminAuthorizedJsonSaveRequest)
        .body(asList(personType, groupType))
        .post("/api/graphs/{graphId}/types?batch=true&mode=insert", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @After
  public void deleteExampleGraphWithTypes() {
    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}/types", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(adminAuthorizedRequest)
        .delete("/api/graphs/{id}", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void documentGetTypeById() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-a-type",
            operationIntro("Get a type by id in given graph."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("id")
                    .description("Type identifier (matches `" + CODE + "`)"),
                subsectionWithPath("graph")
                    .description("Graph identifier object"),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the type."),
                fieldWithPath("index")
                    .description("Ordinal number for the type."),
                fieldWithPath("nodeCodePrefix")
                    .description("Optional prefix for node codes of this type."),
                subsectionWithPath("permissions")
                    .description("Optional map of type permissions where keys are graph roles and "
                        + "values are lists of permissions. Permissions are returned only for "
                        + "admin users."),
                subsectionWithPath("properties")
                    .description("Optional map of type properties where keys are property ids "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."),
                subsectionWithPath("textAttributes")
                    .description("Optional array of text attributes defined for the type."),
                subsectionWithPath("referenceAttributes")
                    .description("Optional array of reference attributes defined for the type."))))
        .filter(document("get-a-type-text-attribute",
            relaxedResponseFields(
                fieldWithPath("textAttributes[].id")
                    .attributes(key("displayName").value("id"))
                    .description("Text attribute identifier (matches `" + CODE + "`)."),
                fieldWithPath("textAttributes[].regex")
                    .attributes(key("displayName").value("regex"))
                    .description("Regular expression defining value range for the text attribute."),
                fieldWithPath("textAttributes[].uri")
                    .attributes(key("displayName").value("uri"))
                    .description("Optional identifying URI for the text attribute."),
                subsectionWithPath("textAttributes[].permissions")
                    .attributes(key("displayName").value("permissions"))
                    .description("Optional map of attribute permissions. Keys are a graph roles. "
                        + "Values are lists of permissions where a permission is one of "
                        + "`INSERT`, `READ`, `UPDATE`, `DELETE`. Only application admins can "
                        + "update permissions."),
                subsectionWithPath("textAttributes[].properties")
                    .attributes(key("displayName").value("properties"))
                    .description("Optional map of attribute properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Text Attribute\" }`)"))))
        .filter(document("get-a-type-reference-attribute",
            relaxedResponseFields(
                fieldWithPath("referenceAttributes[].id")
                    .attributes(key("displayName").value("id"))
                    .description(
                        "Reference attribute identifier (matches `" + CODE + "`)."),
                fieldWithPath("referenceAttributes[].range")
                    .attributes(key("displayName").value("range"))
                    .description("Type identifier defining the value range for the reference "
                        + "attribute."),
                fieldWithPath("referenceAttributes[].uri")
                    .attributes(key("displayName").value("uri"))
                    .description("Optional identifying URI for the reference attribute."),
                subsectionWithPath("referenceAttributes[].permissions")
                    .attributes(key("displayName").value("permissions"))
                    .description("Optional map of attribute permissions. Keys are a graph roles. "
                        + "Values are lists of permissions where a permission is one of "
                        + "`INSERT`, `READ`, `UPDATE`, `DELETE`. Only application admins can "
                        + "update permissions."),
                subsectionWithPath("referenceAttributes[].properties")
                    .attributes(key("displayName").value("properties"))
                    .description("Optional map of attribute properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Reference Attribute\" }`)"))))
        .when()
        .get("/api/graphs/{graphId}/types/{id}", personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetGraphTypes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-graph-types",
            operationIntro(
                "Returns an array containing all types in given graph. Roles and permissions "
                    + "are visible for admin users only."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .when()
        .get("/api/graphs/{graphId}/types", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllTypes() {
    given(adminAuthorizedJsonGetRequest)
        .filter(document("get-all-types",
            operationIntro(
                "Returns an array containing all types visible to the user. Roles and permissions "
                    + "are visible for admin users only.")))
        .when()
        .get("/api/types")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveType() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-type",
            operationIntro("On success, operation returns the saved type."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of type objects is expected. Multiple types are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed.")),
            requestFields(
                fieldWithPath("id")
                    .description("*Required.* Type identifier (matches `" + CODE + "`)."),
                fieldWithPath("index")
                    .optional()
                    .ignored(),
                subsectionWithPath("graph")
                    .optional()
                    .ignored(),
                fieldWithPath("nodeCodePrefix")
                    .description("Optional prefix for node codes of this type. For example prefix "
                        + "`P-` would produce nodes with codes `P-123`. If none is specified, "
                        + "by default nodes codes are prefixed with type id converted to lower "
                        + "snake-case."),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the type. URI must be unique within "
                        + "the graph."),
                subsectionWithPath("permissions")
                    .description("Optional map of type permissions. Keys are a graph roles. "
                        + "Values are lists of permissions where a permission is one of "
                        + "`INSERT`, `READ`, `UPDATE`, `DELETE`. Only application admins can "
                        + "update permissions."),
                subsectionWithPath("properties")
                    .description("Optional map of type properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Type\" }`)"),
                subsectionWithPath("textAttributes")
                    .description("Optional array of text attributes defined for the type."),
                subsectionWithPath("referenceAttributes")
                    .description("Optional array of reference attributes defined for the type."))))
        .filter(document("save-a-type-text-attribute",
            relaxedRequestFields(
                fieldWithPath("textAttributes[].id")
                    .attributes(key("displayName").value("id"))
                    .description("*Required.* Text attribute identifier (matches `" + CODE + "`)."),
                fieldWithPath("textAttributes[].regex")
                    .attributes(key("displayName").value("regex"))
                    .description("Optional regular expression defining value range for "
                        + "the text attribute. If none is provided catch-all regex `" + ALL
                        + "` is set."),
                fieldWithPath("textAttributes[].uri")
                    .attributes(key("displayName").value("uri"))
                    .description("Optional identifying URI for the text attribute. URI must be "
                        + "unique within the type."),
                subsectionWithPath("textAttributes[].permissions")
                    .attributes(key("displayName").value("permissions"))
                    .description("Optional map of attribute permissions. Keys are a graph roles. "
                        + "Values are lists of permissions where a permission is one of "
                        + "`INSERT`, `READ`, `UPDATE`, `DELETE`. Only application admins can "
                        + "update permissions."),
                subsectionWithPath("textAttributes[].properties")
                    .attributes(key("displayName").value("properties"))
                    .description("Optional map of attribute properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Text Attribute\" }`)"))))
        .filter(document("save-a-type-reference-attribute",
            relaxedRequestFields(
                fieldWithPath("referenceAttributes[].id")
                    .attributes(key("displayName").value("id"))
                    .description(
                        "*Required.* Reference attribute identifier (matches `" + CODE + "`)."),
                fieldWithPath("referenceAttributes[].range")
                    .attributes(key("displayName").value("range"))
                    .description("Optional type identifier defining the value range for "
                        + "the reference attribute. Range object has fields `id` and optionally "
                        + "`graph.id` defining the type. If range is not provided, current "
                        + "domain (i.e. attribute owning type) is assumed."),
                fieldWithPath("referenceAttributes[].uri")
                    .attributes(key("displayName").value("uri"))
                    .description(
                        "Optional identifying URI for the reference attribute. URI must be "
                            + "unique within the type."),
                subsectionWithPath("referenceAttributes[].permissions")
                    .attributes(key("displayName").value("permissions"))
                    .description("Optional map of attribute permissions. Keys are a graph roles. "
                        + "Values are lists of permissions where a permission is one of "
                        + "`INSERT`, `READ`, `UPDATE`, `DELETE`. Only application admins can "
                        + "update permissions."),
                subsectionWithPath("referenceAttributes[].properties")
                    .attributes(key("displayName").value("properties"))
                    .description("Optional map of attribute properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Reference Attribute\" }`)"))))
        .when()
        .body(personType)
        .post("/api/graphs/{graphId}/types", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveTypeUsingPut() {
    given(adminAuthorizedJsonSaveRequest)
        .filter(document("save-a-type-using-put", operationIntro(
            "Saving using `PUT` is also supported. Type id is given as a path parameter.\n"
                + "On success, operation will return the saved type."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)"))))
        .when()
        .body(personType)
        .put("/api/graphs/{graphId}/types/{id}", personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentDeleteType() {
    given(adminAuthorizedRequest)
        .filter(document("delete-a-type", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A type can't be deleted if it contains any data (nodes)."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)"))))
        .when()
        .delete("/api/graphs/{graphId}/types/{id}", personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
