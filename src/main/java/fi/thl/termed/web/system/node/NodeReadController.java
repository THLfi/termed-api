package fi.thl.termed.web.system.node;

import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByAnyPropertyPrefix;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.Gson;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.json.JsonStream;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeReadController {

  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Gson gson;

  @GetJsonMapping("/nodes")
  public void get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    Specification<NodeId, Node> spec = typeService.getValues(user)
        .map(type -> specifyByAnyPropertyPrefix(type, query))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    response.setContentType(APPLICATION_JSON_UTF8_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    JsonStream.write(response.getOutputStream(), gson,
        nodeService.getValues(new Query<>(spec, sort, max), user), Node.class);
  }

  @GetJsonMapping("/graphs/{graphId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = typeService.getValues(new TypesByGraphId(graphId), user)
        .map(type -> specifyByAnyPropertyPrefix(type, query))
        .collect(OrSpecification::new, OrSpecification::or, OrSpecification::or);

    response.setContentType(APPLICATION_JSON_UTF8_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    JsonStream.write(response.getOutputStream(), gson,
        nodeService.getValues(new Query<>(spec, sort, max), user), Node.class);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    Type type = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByAnyPropertyPrefix(type, query);

    response.setContentType(APPLICATION_JSON_UTF8_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    JsonStream.write(response.getOutputStream(), gson,
        nodeService.getValues(new Query<>(spec, sort, max), user), Node.class);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  public Node get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User user) {

    return nodeService.get(new NodeId(id, typeId, graphId), user)
        .orElseThrow(NotFoundException::new);
  }

}
