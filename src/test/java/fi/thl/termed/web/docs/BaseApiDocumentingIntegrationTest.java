package fi.thl.termed.web.docs;

import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

import fi.thl.termed.web.BaseApiIntegrationTest;
import io.restassured.filter.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseApiDocumentingIntegrationTest extends BaseApiIntegrationTest {

  @BeforeEach
  public void addDocumentationFilterToBaseRequestSpecifications(
      RestDocumentationContextProvider restDocumentation) {

    Filter documentationConfiguration =
        documentationConfiguration(restDocumentation);

    userAuthorizedRequest.filter(documentationConfiguration);
    adminAuthorizedRequest.filter(documentationConfiguration);
    superuserAuthorizedRequest.filter(documentationConfiguration);

    adminAuthorizedJsonGetRequest.filter(documentationConfiguration);
    adminAuthorizedJsonSaveRequest.filter(documentationConfiguration);
  }

}
