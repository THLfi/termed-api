package fi.thl.termed.web;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.domain.AppRole.ADMIN;
import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.domain.AppRole.USER;
import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.mapper.ObjectMapperType.GSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
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

  String testUsername = "user";
  String testPassword = randomAlphanumericString(8);

  String testAdminUsername = "admin";
  String testAdminPassword = randomAlphanumericString(12);

  String testSuperuserUsername = "superuser";
  String testSuperuserPassword = randomAlphanumericString(12);

  @Autowired
  Service<String, User> userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @LocalServerPort
  private int serverPort;

  @Before
  public void setUp() {
    RestAssured.port = serverPort;
    RestAssured.config = config().objectMapperConfig(new ObjectMapperConfig(GSON));

    userRepository.save(of(
        new User(testUsername, passwordEncoder.encode(testPassword), USER),
        new User(testAdminUsername, passwordEncoder.encode(testAdminPassword), ADMIN),
        new User(testSuperuserUsername, passwordEncoder.encode(testSuperuserPassword), SUPERUSER)),
        UPSERT, defaultOpts(), new User("initializer", "", SUPERUSER));
  }

}
