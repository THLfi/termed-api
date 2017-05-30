package fi.thl.termed.web.system.node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.NodeRdfGraphWrapper;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs/{graphId}/nodes/sparql")
public class NodeSparqlReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @PostMapping(produces = MediaTypes.TEXT_CSV_VALUE)
  public ResponseEntity<String> queryCsv(@PathVariable("graphId") UUID graphId,
      @RequestParam(value = "timeout", defaultValue = "10") int timeout,
      @RequestBody String query, @AuthenticationPrincipal User user)
      throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Type> types = typeService.get(new TypesByGraphId(graphId), user).collect(toList());
    Function<Specification<NodeId, Node>, Stream<Node>> nodes = s -> nodeService.get(s, user);

    Model model = createModelForGraph(new NodeRdfGraphWrapper(types, nodes));

    QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(query), model);
    qe.setTimeout(timeout, TimeUnit.SECONDS);
    ResultSet results = qe.execSelect();

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsCSV(out, results);
      return new ResponseEntity<>(out.toString(UTF_8.name()), HttpStatus.OK);
    } catch (QueryCancelledException e) {
      return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
    }
  }

}
