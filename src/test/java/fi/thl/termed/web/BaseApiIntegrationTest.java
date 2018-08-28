package fi.thl.termed.web;

import static com.google.common.base.Charsets.UTF_8;
import static fi.thl.termed.domain.User.newAdmin;
import static fi.thl.termed.domain.User.newSuperuser;
import static fi.thl.termed.domain.User.newUser;
import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.mapper.ObjectMapperType.GSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.google.gson.Gson;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseApiIntegrationTest {

  @Autowired
  private Service<String, User> users;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  Gson gson;

  @LocalServerPort
  private int serverPort;

  private String testUserUsername = "test-user";
  private String testAdminUsername = "test-admin";
  private String testSuperuserUsername = "test-superuser";
  private String testUserPassword = randomAlphanumericString(20);
  private String testAdminPassword = randomAlphanumericString(20);
  private String testSuperuserPassword = randomAlphanumericString(20);

  RequestSpecification userAuthorizedRequest;
  RequestSpecification adminAuthorizedRequest;
  RequestSpecification superuserAuthorizedRequest;

  RequestSpecification adminAuthorizedJsonGetRequest;
  RequestSpecification adminAuthorizedJsonSaveRequest;

  @Before
  public void configRestAssured() {
    RestAssured.port = serverPort;
    RestAssured.config = config()
        .objectMapperConfig(new ObjectMapperConfig(GSON))
        .encoderConfig(encoderConfig().encodeContentTypeAs("application/rdf+xml", ContentType.XML));
  }

  @Before
  public void buildBaseRequestSpecifications() {
    userAuthorizedRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testUserUsername, testUserPassword))
        .build();
    adminAuthorizedRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testAdminUsername, testAdminPassword))
        .build();
    superuserAuthorizedRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testSuperuserUsername, testSuperuserPassword))
        .build();

    adminAuthorizedJsonGetRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testAdminUsername, testAdminPassword))
        .addHeader("Accept", "application/json")
        .build();
    adminAuthorizedJsonSaveRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testAdminUsername, testAdminPassword))
        .addHeader("Content-type", "application/json")
        .build();
  }

  @Before
  public void insertTestUsers() {
    users.save(Stream.of(
        newUser(testUserUsername, encoder.encode(testUserPassword)),
        newAdmin(testAdminUsername, encoder.encode(testAdminPassword)),
        newSuperuser(testSuperuserUsername, encoder.encode(testSuperuserPassword))),
        INSERT,
        defaultOpts(),
        newSuperuser("test-initializer"));
  }

  @After
  public void deleteTestUsers() {
    users.delete(
        Stream.of(testUserUsername, testAdminUsername, testSuperuserUsername),
        defaultOpts(),
        newSuperuser("test-cleaner"));
  }

  private String basicAuth(String username, String password) {
    return "Basic " + encodeBase64(username + ":" + password);
  }

  private String encodeBase64(String str) {
    return Base64.getEncoder().encodeToString(str.getBytes(UTF_8));
  }

}
