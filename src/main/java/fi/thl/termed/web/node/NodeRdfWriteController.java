package fi.thl.termed.web.node;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs/{graphId}/nodes")
public class NodeRdfWriteController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostRdfMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  private void post(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "importCodes", defaultValue = "true") boolean importCodes,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestBody Model model,
      @AuthenticationPrincipal User currentUser) {

    log.info("Importing RDF-model {} (user: {})", graphId, currentUser.getUsername());

    Function<NodeId, Optional<Node>> nodeProvider = id -> nodeService.get(id, currentUser);

    graphService.get(new GraphId(graphId), currentUser).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.getValueStream(new TypesByGraphId(graphId), currentUser).collect(toList());
    List<Node> nodes = new RdfModelToNodes(types, nodeProvider, importCodes)
        .apply(new JenaRdfModel(model));

    nodeService.save(nodes, saveMode(mode), opts(sync), currentUser);
  }

}
