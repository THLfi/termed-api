package fi.thl.termed.web;

import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class UserApiIntegrationTest extends BaseApiIntegrationTest {

  private String exampleUserUsername = "test-user-" + randomAlphanumericString(4);
  private String exampleUserPassword = randomAlphanumericString(8);
  private String exampleUserJson = String.format(
      "{ 'username': '%s', 'password': '%s', 'appRole': 'USER' }",
      exampleUserUsername, exampleUserPassword);

  @Test
  public void regularUserShouldNotBeAbleToAccessUserApi() {
    given(userAuthorizedJsonRequest)
        .body(exampleUserJson)
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(userAuthorizedJsonRequest)
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(userAuthorizedJsonRequest)
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(userAuthorizedJsonRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void adminShouldNotBeAbleToAccessUserApi() {
    given(adminAuthorizedJsonRequest)
        .body(exampleUserJson)
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedJsonRequest)
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedJsonRequest)
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedJsonRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void superuserShouldBeAbleToAccessUserApi() {
    given(superuserAuthorizedJsonRequest)
        .body(exampleUserJson)
        .post("/api/users?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(superuserAuthorizedJsonRequest)
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("username", equalTo(exampleUserUsername))
        .body("password", not(equalTo(exampleUserUsername)))
        .body("appRole", equalTo("USER"));

    given(superuserAuthorizedJsonRequest)
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(superuserAuthorizedJsonRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
