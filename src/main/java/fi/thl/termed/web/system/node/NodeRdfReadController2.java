package fi.thl.termed.web.system.node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.jena.riot.Lang.NTRIPLES;
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
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs/{graphId}/rdf")
public class NodeRdfReadController2 {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @GetMapping(produces = RdfMediaTypes.N_TRIPLES_VALUE)
  public void streamNTriples(@PathVariable(name = "graphId") UUID graphId,
      @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    response.setContentType(RdfMediaTypes.N_TRIPLES_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    try (OutputStream out = response.getOutputStream()) {
      List<Type> types = typeService.get(new TypesByGraphId(graphId), user).collect(toList());
      Function<Specification<NodeId, Node>, Stream<Node>> nodes = s -> nodeService.get(s, user);

      StreamRDFWriter.write(out, new NodeRdfGraphWrapper(types, nodes), NTRIPLES);
    }
  }

  @GetMapping(produces = RdfMediaTypes.TURTLE_VALUE)
  public void streamTurtle(@PathVariable(name = "graphId") UUID graphId,
      @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    response.setContentType(RdfMediaTypes.TURTLE_VALUE);
    response.setCharacterEncoding(UTF_8.toString());

    try (OutputStream out = response.getOutputStream()) {
      List<Type> types = typeService.get(new TypesByGraphId(graphId), user).collect(toList());
      Function<Specification<NodeId, Node>, Stream<Node>> nodes = s -> nodeService.get(s, user);

      StreamRDFWriter.write(out, new NodeRdfGraphWrapper(types, nodes), TURTLE);
    }
  }

}
