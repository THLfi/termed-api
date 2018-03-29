package fi.thl.termed.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class UserApiIntegrationTest extends BaseApiIntegrationTest {

  private String exampleUserData = String.format(
      "{ 'username': '%s', 'password': '%s', 'appRole': 'USER' }",
      testUsername, testPassword);

  @Test
  public void regularUserShouldNotBeAbleToAccessUserApi() {
    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json").body(exampleUserData)
        .when().post("/api/users")
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when().get("/api/users/{username}", testUsername)
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when().get("/api/users")
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testUsername, testPassword)
        .contentType("application/json")
        .when().delete("/api/users/{username}", testUsername)
        .then().statusCode(HttpStatus.SC_FORBIDDEN);
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
        .when().get("/api/users/{username}", testAdminUsername)
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .when().get("/api/users")
        .then().statusCode(HttpStatus.SC_FORBIDDEN);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .when().delete("/api/users/{username}", testUsername)
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
        .when().get("/api/users/{username}", testUsername)
        .then().statusCode(HttpStatus.SC_OK)
        .body("username", equalTo(testUsername))
        .body("password", not(equalTo(testUsername)))
        .body("appRole", equalTo("USER"));

    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json")
        .when().get("/api/users")
        .then().statusCode(HttpStatus.SC_OK);

    given()
        .auth().basic(testSuperuserUsername, testSuperuserPassword)
        .contentType("application/json")
        .when().delete("/api/users/{username}", testUsername)
        .then().statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
