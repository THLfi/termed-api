package fi.thl.termed.web;

import static com.jayway.restassured.RestAssured.given;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class UserApiIntegrationTest extends BaseApiIntegrationTest {

  private String exampleUserUsername = "user";
  private String exampleUserPassword = UUIDs.randomUUIDString();
  private String exampleUserData = String.format(
      "{ 'username': '%s', 'password': '%s', 'appRole': 'USER' }",
      exampleUserUsername, exampleUserPassword);

  private String testAdminUsername = "testAdmin";
  private String testAdminPassword = UUIDs.randomUUIDString();

  private String testSuperuserUsername = "testSuperuser";
  private String testSuperuserPassword = UUIDs.randomUUIDString();

  @Before
  public void addTestAdminAndSuperuser() {
    User initializer = new User("initializer", "", AppRole.SUPERUSER);

    userRepository.save(new User(testAdminUsername,
        passwordEncoder.encode(testAdminPassword),
        AppRole.ADMIN), UPSERT, defaultOpts(), initializer);

    userRepository.save(new User(testSuperuserUsername,
        passwordEncoder.encode(testSuperuserPassword),
        AppRole.SUPERUSER), UPSERT, defaultOpts(), initializer);
  }

  @Test
  public void adminShouldNotBeAbleToAccessUserApi() {
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json").body(exampleUserData)
        .when().post("/api/users")
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .when().get("/api/users/{username}", exampleUserUsername)
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .when().get("/api/users")
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .when().delete("/api/users/{username}", exampleUserUsername)
        .then().statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void superuserShouldBeAbleToAccessUserApi() {
    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json").body(exampleUserData)
        .when().post("/api/users")
        .then().statusCode(HttpStatus.SC_NO_CONTENT);

    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json")
        .when().get("/api/users/{username}", exampleUserUsername)
        .then().statusCode(HttpStatus.SC_OK)
        .body("username", equalTo(exampleUserUsername))
        .body("password", not(equalTo(exampleUserUsername)))
        .body("appRole", equalTo("USER"));

    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json")
        .when().get("/api/users")
        .then().statusCode(HttpStatus.SC_OK);

    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json")
        .when().delete("/api/users/{username}", exampleUserUsername)
        .then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
