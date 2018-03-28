package fi.thl.termed.docs;

import static com.google.common.base.Charsets.UTF_8;
import static fi.thl.termed.docs.OperationIntroSnippet.operationIntro;
import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.domain.AppRole.USER;
import static fi.thl.termed.domain.Permission.DELETE;
import static fi.thl.termed.domain.Permission.INSERT;
import static fi.thl.termed.domain.Permission.READ;
import static fi.thl.termed.domain.Permission.UPDATE;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.mapper.ObjectMapperType.GSON;
import static java.util.Arrays.asList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors.modifyUris;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.GraphRole;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import java.util.UUID;
import org.apache.http.HttpStatus;
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
public class GraphApiDocsTest {

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  @Autowired
  private Service<String, User> userService;

  @Autowired
  private Service<GraphId, Graph> graphService;

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

  private UUID exampleGraphId = UUIDs.nameUUIDFromString("example-graph");
  private Graph exampleGraph = Graph.builder()
      .id(exampleGraphId)
      .code("example-graph")
      .uri("http://example.org/termed/example-graph/")
      .roles(asList("guest", "admin"))
      .permissions(ImmutableMultimap.<String, Permission>builder()
          .putAll("guest", READ)
          .putAll("admin", READ, INSERT, UPDATE, DELETE).build())
      .properties("prefLabel", LangValue.of("en", "Example Graph"))
      .build();

  private UUID simpleGraphId = UUIDs.nameUUIDFromString("simple-graph");
  private Graph simpleGraph = Graph.builder()
      .id(simpleGraphId)
      .code("simple-graph")
      .roles(asList("guest", "admin"))
      .permissions(ImmutableMultimap.<String, Permission>builder()
          .putAll("guest", READ)
          .putAll("admin", READ, INSERT, UPDATE, DELETE).build())
      .properties("prefLabel", LangValue.of("en", "Simple Graph"))
      .build();

  private UUID anotherGraphId = UUIDs.nameUUIDFromString("another-graph");
  private Graph anotherGraph = Graph.builder()
      .id(anotherGraphId)
      .code("another-graph")
      .roles(asList("guest", "admin"))
      .permissions(ImmutableMultimap.<String, Permission>builder()
          .putAll("guest", READ)
          .putAll("admin", READ, INSERT, UPDATE, DELETE).build())
      .properties("prefLabel", LangValue.of("en", "Another Graph"))
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

    graphService.save(asList(exampleGraph, simpleGraph, anotherGraph),
        SaveMode.UPSERT, defaultOpts(), docsInitializer);

    userService.save(asList(
        new User(
            exampleAdminUsername,
            passwordEncoder.encode(exampleAdminPassword),
            ADMIN),
        new User(
            exampleUserUsername,
            passwordEncoder.encode(exampleUserPassword),
            USER,
            asList(
                new GraphRole(GraphId.of(simpleGraphId), "guest"),
                new GraphRole(GraphId.of(anotherGraphId), "guest")))),
        UPSERT, defaultOpts(), docsInitializer);
  }

  @Test
  public void documentGetGraphById() {
    given(this.spec)
        .filter(document("get-a-graph",
            operationIntro(),
            pathParameters(
                parameterWithName("id")
                    .description("Graph identifier (UUID)")),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            responseFields(
                fieldWithPath("id")
                    .description("Graph identifier (UUID)."),
                fieldWithPath("code")
                    .description("Optional identifying code for the graph."),
                fieldWithPath("uri")
                    .description("Optional identifying URI for the type."),
                fieldWithPath("roles")
                    .description("Optional list of roles defined for the graph. "
                        + "Roles are returned only for admin users."),
                subsectionWithPath("permissions")
                    .description("Optional map of graph permissions where keys are graph roles and "
                        + "values are lists of permissions. Permissions are returned only for "
                        + "admin users."),
                subsectionWithPath("properties")
                    .description("Optional map of graph properties where keys are property ids "
                        + "(e.g. `prefLabel`) and values are a lists of localized values."))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Accept", "application/json")
        .when()
        .get("/api/graphs/{id}", exampleGraphId)
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentGetAllGraphs() {
    given(this.spec)
        .filter(document("get-all-graphs", operationIntro(
            "Returns an array containing all graphs visible to the user. Roles and permissions "
                + "are visible for admin users only.")))
        .header("Authorization", basic(exampleUserUsername, exampleUserPassword))
        .header("Accept", "application/json")
        .when()
        .get("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveGraph() {
    given(this.spec)
        .filter(document("save-a-graph",
            operationIntro("If posted object contains an id, a graph is either updated "
                + "or inserted with the given id. If id is not present, graph is saved with "
                + "new random id.\n\nOn success, operation returns the saved graph."),
            requestHeaders(
                headerWithName("Authorization")
                    .description("Basic authentication credentials")),
            requestParameters(
                parameterWithName("mode").optional()
                    .description("Optional save mode. Supported modes are `insert`, `update`, "
                        + "`upsert`. If mode is not specified, `upsert` is used."),
                parameterWithName("batch").optional()
                    .description("Optional boolean flag for batch mode. If batch is `true`, an "
                        + "array of graph objects is expected. Multiple graphs are saved in one "
                        + "transaction. On success `204` is returned with an empty body. "
                        + "If parameter is not specified, `false` is assumed.")),
            requestFields(
                fieldWithPath("id")
                    .description("Graph identifier (UUID). If an id is provided, existing graph is "
                        + "updated or a new one is created with given id. If id is not given, a new "
                        + "graph is created with random id."),
                fieldWithPath("code")
                    .description("Optional identifying code for the graph. Code must be "
                        + "unique and match pattern `" + CODE + "`"),
                fieldWithPath("uri")
                    .description("Optional identifying uri for the graph. URI must be unique."),
                fieldWithPath("roles")
                    .description("Optional list of roles defined for the graph. "
                        + "A role must match pattern `" + CODE + "`. "
                        + "Only application admins can update graph roles."),
                subsectionWithPath("permissions")
                    .description("Optional map of graph permissions. Keys are a graph roles and "
                        + "must be included in roles list. Values are lists of permissions where "
                        + "a permission is one of `INSERT`, `READ`, `UPDATE`, `DELETE`. "
                        + "Only application admins can update graph permissions."),
                subsectionWithPath("properties")
                    .description("Optional map of graph properties. Keys are property ids "
                        + "(e.g. `prefLabel`, list of properties is available at "
                        + "`GET /api/properties`). Values are lists of lang value objects (e.g. "
                        + "`{ \"lang\": \"en\", \"value\": \"Example Graph\" }`)"))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Content-Type", "application/json")
        .body(gson.toJson(exampleGraph))
        .when()
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentSaveGraphUsingPut() {
    given(this.spec)
        .filter(document("save-a-graph-using-put", operationIntro(
            "Saving using `PUT` is also supported. Graph id is given as a path parameter.\n"
                + "On success, operation will return the saved graph."),
            pathParameters(parameterWithName("id").description("Graph identifier (UUID)"))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .header("Content-Type", "application/json")
        .body(gson.toJson(exampleGraph))
        .when()
        .put("/api/graphs/{id}", exampleGraphId)
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void documentDeleteGraph() {
    given(this.spec)
        .filter(document("delete-a-graph", operationIntro(
            "On success, operation will return `204` with an empty body.\n\n"
                + "A graph can't be deleted if it contains any data (types or nodes)."),
            pathParameters(parameterWithName("id").description("Graph identifier (UUID)"))))
        .header("Authorization", basic(exampleAdminUsername, exampleAdminPassword))
        .when()
        .delete("/api/graphs/{id}", exampleGraphId)
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
