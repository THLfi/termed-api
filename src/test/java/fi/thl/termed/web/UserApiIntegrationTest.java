package fi.thl.termed.web;

import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

public class UserApiIntegrationTest extends BaseApiIntegrationTest {

  private String exampleUserUsername = "test-user-" + randomAlphanumericString(4);
  private String exampleUserPassword = randomAlphanumericString(8);
  private String exampleUserJson = String.format(
      "{ 'username': '%s', 'password': '%s', 'appRole': 'USER' }",
      exampleUserUsername, exampleUserPassword);

  @Test
  public void regularUserShouldNotBeAbleToAccessUserApi() {
    given(userAuthorizedRequest)
        .contentType("application/json")
        .body(exampleUserJson)
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(userAuthorizedRequest)
        .accept("application/json")
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    given(userAuthorizedRequest)
        .accept("application/json")
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("isEmpty()", Matchers.is(true));

    given(userAuthorizedRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void adminShouldNotBeAbleToAccessUserApi() {
    given(adminAuthorizedRequest)
        .contentType("application/json")
        .body(exampleUserJson)
        .post("/api/users")
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    given(adminAuthorizedRequest)
        .accept("application/json")
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    given(adminAuthorizedRequest)
        .accept("application/json")
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("isEmpty()", Matchers.is(true));

    given(adminAuthorizedRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void superuserShouldBeAbleToAccessUserApi() {
    given(superuserAuthorizedRequest)
        .contentType("application/json")
        .body(exampleUserJson)
        .post("/api/users?mode=insert")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    given(superuserAuthorizedRequest)
        .accept("application/json")
        .get("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("username", equalTo(exampleUserUsername))
        .body("password", equalTo(""))
        .body("appRole", equalTo("USER"));

    given(superuserAuthorizedRequest)
        .accept("application/json")
        .get("/api/users")
        .then()
        .statusCode(HttpStatus.SC_OK);

    given(superuserAuthorizedRequest)
        .delete("/api/users/{username}", exampleUserUsername)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

}
