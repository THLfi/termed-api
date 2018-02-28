package fi.thl.termed.docs;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

public class OperationIntroSnippet extends TemplatedSnippet {

  private OperationIntroSnippet(String description) {
    super("operation-intro", ImmutableMap.of("description", description));
  }

  public static OperationIntroSnippet operationIntro() {
    return new OperationIntroSnippet("");
  }

  public static OperationIntroSnippet operationIntro(String description) {
    return new OperationIntroSnippet(description);
  }

  @Override
  protected Map<String, Object> createModel(Operation operation) {
    Map<String, Object> model = new HashMap<>();

    model.put("method", operation.getRequest().getMethod());
    model.put("path", operation.getAttributes()
        .get(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE));

    return model;
  }

}
