package fi.thl.termed.web.system.node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.riot.Lang.TURTLE;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.NodeRdfGraphWrapper;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.rdf.RdfMediaTypes;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  private Model buildModelWrapper(UUID graphId, User user) {
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.getValues(new TypesByGraphId(graphId), user).collect(toList());
    Function<Specification<NodeId, Node>, Stream<Node>> nodes = s -> nodeService.getValues(s, user);
    return createModelForGraph(new NodeRdfGraphWrapper(types, nodes));
  }

  @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> queryText(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "timeout", defaultValue = "10") int timeout,
      @RequestBody String sparqlSelect,
      @AuthenticationPrincipal User user)
      throws IOException {

    Model model = buildModelWrapper(graphId, user);

    QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(sparqlSelect), model);
    qe.setTimeout(timeout, TimeUnit.SECONDS);
    ResultSet results = qe.execSelect();

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ResultSetFormatter.out(out, results);
      return new ResponseEntity<>(out.toString(UTF_8.name()), HttpStatus.OK);
    } catch (QueryCancelledException e) {
      return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
    }
  }

  @PostMapping(produces = MediaTypes.TEXT_CSV_VALUE)
  public ResponseEntity<String> queryCsv(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "timeout", defaultValue = "10") int timeout,
      @RequestBody String sparqlSelect,
      @AuthenticationPrincipal User user)
      throws IOException {

    Model model = buildModelWrapper(graphId, user);

    QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(sparqlSelect), model);
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

  @PostMapping(params = "postProcess=true", produces = RdfMediaTypes.TURTLE_VALUE)
  public void exportAndTransform(
      @PathVariable("graphId") UUID graphId,
      @RequestBody String sparqlUpdate,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    Model model = ModelFactory.createDefaultModel().add(buildModelWrapper(graphId, user));

    UpdateAction.execute(UpdateFactory.create(sparqlUpdate), model);

    try (OutputStream out = response.getOutputStream()) {
      response.setContentType(RdfMediaTypes.TURTLE_VALUE);
      response.setCharacterEncoding(UTF_8.toString());

      StreamRDFWriter.write(out, model.getGraph(), TURTLE);
    }
  }

}
