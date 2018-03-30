package fi.thl.termed.web;

import static com.google.common.base.Charsets.UTF_8;
import static fi.thl.termed.domain.User.newAdmin;
import static fi.thl.termed.domain.User.newSuperuser;
import static fi.thl.termed.domain.User.newUser;
import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.mapper.ObjectMapperType.GSON;
import static java.util.Arrays.asList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseApiIntegrationTest {

  @Autowired
  private Service<String, User> users;

  @Autowired
  private PasswordEncoder encoder;

  @LocalServerPort
  private int serverPort;

  private String testUserUsername = "test-user";
  private String testAdminUsername = "test-admin";
  private String testSuperuserUsername = "test-superuser";
  private String testUserPassword = randomAlphanumericString(20);
  private String testAdminPassword = randomAlphanumericString(20);
  private String testSuperuserPassword = randomAlphanumericString(20);

  RequestSpecification userAuthorizedJsonRequest;
  RequestSpecification adminAuthorizedJsonRequest;
  RequestSpecification superuserAuthorizedJsonRequest;

  @Before
  public void configRestAssured() {
    RestAssured.port = serverPort;
    RestAssured.config = config().objectMapperConfig(new ObjectMapperConfig(GSON));
  }

  @Before
  public void buildBaseRequestSpecifications() {
    userAuthorizedJsonRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testUserUsername, testUserPassword))
        .addHeader("Content-type", "application/json")
        .addHeader("Accept", "application/json")
        .build();

    adminAuthorizedJsonRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testAdminUsername, testAdminPassword))
        .addHeader("Content-type", "application/json")
        .addHeader("Accept", "application/json")
        .build();

    superuserAuthorizedJsonRequest = new RequestSpecBuilder()
        .addHeader("Authorization", basicAuth(testSuperuserUsername, testSuperuserPassword))
        .addHeader("Content-type", "application/json")
        .addHeader("Accept", "application/json")
        .build();
  }

  @Before
  public void insertTestUsers() {
    users.save(asList(
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
        asList(testUserUsername, testAdminUsername, testSuperuserUsername),
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
