package fi.thl.termed.web;

import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.Service;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseApiIntegrationTest {

  protected String testUsername = "test";
  protected String testPassword = UUIDs.randomUUIDString();

  @Value("${local.server.port}")
  protected int serverPort;
  @Resource
  protected Service<String, User> userRepository;
  @Resource
  protected PasswordEncoder passwordEncoder;

  @Before
  public void setUp() {
    RestAssured.port = serverPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig(ObjectMapperType.GSON));

    userRepository.save(
        new User(testUsername, passwordEncoder.encode(testPassword), AppRole.ADMIN),
        UPSERT, defaultOpts(), new User("initializer", "", AppRole.SUPERUSER));
  }

}
