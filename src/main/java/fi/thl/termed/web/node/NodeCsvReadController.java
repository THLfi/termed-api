package fi.thl.termed.web.node;

import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.select.Selects;
import fi.thl.termed.service.node.util.NodesToCsv;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetCsvMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeCsvReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetCsvMapping("/nodes")
  public void get(
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, types, where);
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        NodesToCsv.writeAsCsv(nodes, selects, out);
      }
    }
  }

  @GetCsvMapping("/graphs/{graphId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    if (!graphService.exists(new GraphId(graphId), user)) {
      throw new NotFoundException();
    }

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);
    List<Type> anyDomain = typeService.getValues(new TypesByGraphId(graphId), user);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, anyDomain, where);
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        NodesToCsv.writeAsCsv(nodes, selects, out);
      }
    }
  }

  @GetCsvMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, where);
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        NodesToCsv.writeAsCsv(nodes, selects, out);
      }
    }
  }

}
