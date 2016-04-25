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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;

import fi.thl.termed.Application;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.Repository;
import fi.thl.termed.util.UUIDs;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public abstract class BaseApiIntegrationTest {

  protected String username;
  protected String password;

  @Value("${local.server.port}")
  private int serverPort;
  @Resource
  private Repository<String, User> userRepository;

  @Before
  public void setUp() {
    RestAssured.port = serverPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig(ObjectMapperType.GSON));

    username = "test";
    password = new BCryptPasswordEncoder().encode(UUIDs.randomUUIDString());

    userRepository.save(new User(username,
                                 new BCryptPasswordEncoder().encode(password),
                                 "ADMIN"));
  }

}
