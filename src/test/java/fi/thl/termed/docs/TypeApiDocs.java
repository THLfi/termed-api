package fi.thl.termed.docs;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.docs.OperationIntroSnippet.operationIntro;
import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.domain.AppRole.USER;
import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static fi.thl.termed.util.RegularExpressions.ALL;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.SIMPLE_EMAIL;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.mapper.ObjectMapperType.GSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors.modifyUris;
import static org.springframework.restdocs.snippet.Attributes.key;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import org.apache.http.HttpStatus;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TypeApiDocs {

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  @Autowired
  private Service<String, User> userService;

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Gson gson;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @LocalServerPort
  private int serverPort;

  private RequestSpecification spec;

  private String exampleAdminUsername = "admin";
  private String exampleAdminPassword = "s3cret";

  private String exampleUserUsername = "user";
  private String exampleUserPassword = "passw0rd";

  private Multimap<String, Permission> examplePermissions = ImmutableMultimap.<String, Permission>builder()
      .putAll("guest", READ)
      .putAll("admin", READ, INSERT, UPDATE, DELETE).build();

  private GraphId exampleGraphId = GraphId.of(UUIDs.nameUUIDFromString("example-graph"));
  private Graph exampleGraph = Graph.builder()
      .id(exampleGraphId)
      .roles(of("guest", "admin"))
      .permissions(examplePermissions)
      .build();

  private TypeId personTypeId = TypeId.of("Person", exampleGraphId);
  private Type personType = Type.builder()
      .id(personTypeId)
      .uri(FOAF.Person.getURI())
      .permissions(examplePermissions)
      .properties("prefLabel", LangValue.of("en", "Person"))
      .textAttributes(
          TextAttribute.builder()
              .id("name", personTypeId)
              .regex("^\\w+$")
              .uri(FOAF.name.getURI())
              .permissions(examplePermissions)
              .properties("prefLabel", LangValue.of("en", "Name"))
              .build(),
          TextAttribute.builder()
              .id("email", personTypeId)
              .regex("^" + SIMPLE_EMAIL + "$")
              .uri(FOAF.mbox.getURI())
              .permissions(examplePermissions)
              .properties("prefLabel", LangValue.of("en", "E-mail"))
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("knows", personTypeId)
              .range(personTypeId)
              .uri(FOAF.knows.getURI())
              .permissions(examplePermissions)
              .properties("prefLabel", LangValue.of("en", "Knows"))
              .build())
      .build();

  private TypeId groupTypeId = TypeId.of("Group", exampleGraphId);
  private Type groupType = Type.builder()
      .id(groupTypeId)
      .permissions(examplePermissions)
      .properties("prefLabel", LangValue.of("en", "Group"))
      .textAttributes(
          TextAttribute.builder()
              .id("name", groupTypeId)
              .regex("^\\w+$")
              .permissions(examplePermissions)
              .properties("prefLabel", LangValue.of("en", "Name"))
              .build())
      .referenceAttributes(
          ReferenceAttribute.builder()
              .id("member", groupTypeId)
              .range(personTypeId)
              .permissions(examplePermissions)
              .properties("prefLabel", LangValue.of("en", "Member"))
              .build())
      .build();


  @Before
  public void setUp() {
    RestAssured.port = serverPort;
    RestAssured.config = config().objectMapperConfig(new ObjectMapperConfig(GSON));

    this.spec = new RequestSpecBuilder()
        .addFilter(documentationConfiguration(this.restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(modifyUris().port(8080))
            .withResponseDefaults(modifyUris().port(8080)))
        .build();

    User docsInitializer = new User("docs-initializer", "", SUPERUSER);

    graphService.save(exampleGraph, SaveMode.UPSERT, defaultOpts(), docsInitializer);
    typeService.save(of(personType, groupType), SaveMode.UPSERT, defaultOpts(), docsInitializer);

    userService.save(of(
        new User(
            exampleAdminUsername,
            passwordEncoder.encode(exampleAdminPassword),
            ADMIN),
        new User(
            exampleUserUsername,
            passwordEncoder.encode(exampleUserPassword),
            USER,
            of(new GraphRole(GraphId.of(exampleGraph), "guest")))),
        UPSERT, defaultOpts(), docsInitializer);
  }

  @After
  public void tearDown() {
    User docsCleaner = new User("docs-cleaner", "", SUPERUSER);
    typeService.delete(of(personTypeId, groupTypeId), defaultOpts(), docsCleaner);
    graphService.delete(exampleGraphId, defaultOpts(), docsCleaner);
  }

  @Test
  public void documentGetTypeById() {
    given(this.spec)
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
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Accept", "application/json")
        .when()
        .get("/api/graphs/{graphId}/types/{id}",
            personTypeId.getGraphId(),
            personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetGraphTypes() {
    given(this.spec)
        .filter(document("get-graph-types",
            operationIntro(
                "Returns an array containing all types in given graph. Roles and permissions "
                    + "are visible for admin users only."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"))))
        .header("Authorization", basic(exampleUserUsername, exampleUserPassword))
        .header("Accept", "application/json")
        .when()
        .get("/api/graphs/{graphId}/types", exampleGraphId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllTypes() {
    given(this.spec)
        .filter(document("get-all-types",
            operationIntro(
                "Returns an array containing all types visible to the user. Roles and permissions "
                    + "are visible for admin users only.")))
        .header("Authorization", basic(exampleUserUsername, exampleUserPassword))
        .header("Accept", "application/json")
        .when()
        .get("/api/types")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveType() {
    given(this.spec)
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
                        + "`{ \"lang\": \"en\", \"value\": \"Example Text Attribute\" }`)"))
        ))
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
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Content-Type", "application/json")
        .body(gson.toJson(personType))
        .when()
        .post("/api/graphs/{graphId}/types", personTypeId.getGraphId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveTypeUsingPut() {
    given(this.spec)
        .filter(document("save-a-type-using-put", operationIntro(
            "Saving using `PUT` is also supported. Type id is given as a path parameter.\n"
                + "On success, operation will return the saved type."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)"))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Content-Type", "application/json")
        .body(gson.toJson(personType))
        .when()
        .put("/api/graphs/{graphId}/types/{id}", personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentDeleteGraph() {
    given(this.spec)
        .filter(document("delete-a-type", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A type can't be deleted if it contains any data (nodes)."),
            pathParameters(
                parameterWithName("graphId")
                    .description("Graph identifier (UUID)"),
                parameterWithName("id")
                    .description("Type identifier (matches `" + CODE + "`)"))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .when()
        .delete("/api/graphs/{graphId}/types/{id}", personTypeId.getGraphId(), personTypeId.getId())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  private String basic(String username, String password) {
    return "Basic " + encodeBase64(username + ":" + password);
  }

  private String encodeBase64(String str) {
    return Base64.getEncoder().encodeToString(str.getBytes(UTF_8));
  }

}
