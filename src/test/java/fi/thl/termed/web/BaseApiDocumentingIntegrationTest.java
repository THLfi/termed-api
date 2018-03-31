package fi.thl.termed.web;

import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors.modifyUris;

import com.google.gson.Gson;
import io.restassured.filter.Filter;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.JUnitRestDocumentation;

public abstract class BaseApiDocumentingIntegrationTest extends BaseApiIntegrationTest {

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  @Autowired
  Gson gson;

  @Before
  public void addDocumentationFilterToBaseRequestSpecifications() {
    Filter documentationConfiguration =
        documentationConfiguration(this.restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(modifyUris().port(8080))
            .withResponseDefaults(modifyUris().port(8080));

    userAuthorizedRequest.filter(documentationConfiguration);
    adminAuthorizedRequest.filter(documentationConfiguration);
    superuserAuthorizedRequest.filter(documentationConfiguration);

    adminAuthorizedJsonGetRequest.filter(documentationConfiguration);
    adminAuthorizedJsonSaveRequest.filter(documentationConfiguration);
  }

}
