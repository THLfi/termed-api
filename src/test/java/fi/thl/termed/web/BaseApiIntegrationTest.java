package fi.thl.termed.web;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.internal.mapper.ObjectMapperType;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;

import fi.thl.termed.Application;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.util.UUIDs;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public abstract class BaseApiIntegrationTest {

  protected String testUsername = "test";
  protected String testPassword = UUIDs.randomUUIDString();

  @Value("${local.server.port}")
  protected int serverPort;
  @Resource
  protected Repository<String, User> userRepository;
  @Resource
  protected PasswordEncoder passwordEncoder;

  @Before
  public void setUp() {
    RestAssured.port = serverPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig(ObjectMapperType.GSON));

    userRepository.save(
        new User(testUsername, passwordEncoder.encode(testPassword), AppRole.ADMIN),
        new User("initializer", "", AppRole.SUPERUSER));
  }

}
